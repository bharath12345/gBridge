package in.bharath.gbridge

import akka.actor.{Props, Actor}
import scala.xml.{XML, Node}
import akka.event.LoggingReceive
import spray.json.{JsString, JsObject}
import akka.event.slf4j.SLF4JLogging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

/**
 * Created by bharadwaj on 20/12/13.
 */
object GmondDataParser {
  case class DataXml(pollCounter: Long, lines: List[String])
}

class GmondDataParser(clusterName: String, hostIP: String, hostName: String, port: Int) extends Actor with SLF4JLogging {
  import GmondDataParser._
  import ExecutionContext.Implicits.global
  import in.bharath.gbridge.GmondPoller.{PollRequest}
  import in.bharath.gbridge.JsonPublisher.JsonData

  def attributeEquals(name: String, value: String)(node: Node) = node.attribute(name).exists(n => n.text == value)

  def gcd(a: Int, b: Int): Int = if (b == 0) a.abs else gcd(b, a % b)

  val jsonPublisher = context.actorOf(Props[JsonPublisher], name = "JsonPublisher")

  def receive = LoggingReceive {

    case DataXml(pollCounter, lines) => {

      val relevant = lines.dropWhile(line => line.contains("<GANGLIA_XML") == false)
      //for(line <- relevant) log.debug(s"line ==> $line")

      val singleBlob = relevant.flatten.mkString
      //log.debug(singleBlob)

      val xml = XML.loadString(singleBlob)

      /*
       ToDo: The point is to publish JSON of each metric onto ZeroMQ concurrently
       Never maintain any state of previous values - no maps, arrays etc - nothing like that
       However one info that will have to be maintained is the polling interval of different metrics per node per cluster
         Use the polling interval value to filter out all other cluster/nodes when u get the response
       One problem - not all polling intervals will be in multiples...
         one way to tackle this problem is to find the GCD of all the polling intervals and poll gmond per the GCD
         but the shortcoming of this approach is that, doing this, gmond will be polled too frequently

       The JSON to be published on the ZeroMQ bus is a stub of the following structure -
            {
              cluster  : "clusterName",
              hostIP     : "hostName",
              ip       : "hostIP",
              instance : "instanceIdentifier",  => This field is useful when there are multiple metrics of the same type on a system
              metric   : "metricName",
              value    : "metricValue",
              type     : "metricType",
              units    : "metricUnits",
              period   : "metricCollectionPeriod"
            }

        - Poll gmond per the GCD
        - Keep a counter per polling cycle
        - Compare the counter with pollingCycles (which is polling-period divided by GCD)
        - Whenever the counter matches, read value and publish on ZeroMQ
        - Every cycle, update the pollingCycles as well (user might have changed the time to poll for a certain metric)
        - If there is a change in the pollingCycles for a metric, then update it and publish the first value immediately

        - Actor way of designing this?
        - Separate Actors each for
          * Polling gmond and pass on the XML - note, the TCP request/response should NOT block the actor but work in future
          * Collect gmond xml, parse it and create json
          * Publish json to zeromq
      */

      val clusterXML = (xml \\ "CLUSTER").filter(attributeEquals("NAME", clusterName))
      //log.debug(s"cluster = $clusterXML")

      val nodeXML = (clusterXML \\ "HOST").filter(attributeEquals("IP", hostName))
      //192.168.1.113 or 10.50.1.235 or 192.168.1.5
      //log.debug(s"node = $nodeXML")

      //val clusterName = (clusterXML \ "@NAME").text
      //val hostName = (nodeXML \ "@NAME").text

      val metricTuples = (nodeXML \ "METRIC").map(x => ((x \ "@NAME").text, (x \ "@TMAX").text.toInt))
      //log.debug("metric tuples = " + metricTuples)

      val periods: List[Int] = metricTuples.map(t => t._2).toList
      val periodGCD = periods.foldLeft(0)(gcd(_, _))
      if(pollCounter == 0) log.debug(s"gcd of periods = $periodGCD")

      val pollingCycles = metricTuples.map(t => (t._1, t._2 / periodGCD))
      if(pollCounter == 0) log.debug(s"poll cycles = $pollingCycles")

      // This for loop will publish Json only when the polling cycle of a metric satisfies.
      // In the very first iteration, the pollCounter will be 0, and thus all the metrics will be published
      for {
        p <- pollingCycles
        m = p._1
        if(pollCounter % pollingCycles.filter(x => x._1 == m)(0)._2 == 0)
      } {
        val metricXML = (nodeXML \ "METRIC").filter(attributeEquals("NAME", m))
        //log.debug(s"metric values = " + metricValue)

        val singleMetric = JsObject(
          "cluster" -> JsString(clusterName),
          "host" -> JsString(hostName),
          "ip" -> JsString(hostIP),
          "instance" -> JsString(""),
          "metric" -> JsString(m),
          "value" -> JsString((metricXML \ "@VAL").text),
          "type" -> JsString((metricXML \ "@TYPE").text),
          "units" -> JsString((metricXML \ "@UNITS").text),
          "period" -> JsString((metricXML \ "@TMAX").text)
        )

        log.debug(s"json = " + singleMetric)

        // Send this JSON onwards to ZeroMQ
        //jsonPublisher ! JsonData(singleMetric)
      }

      val gmondPollerReference = sender

      /*
        This iteration of poll is complete. Notify back the GmondPoller so that it schedules the next poll
        of this instance... but only after sleep waiting for GCD seconds
       */
      context.system.scheduler.scheduleOnce(periodGCD.seconds) {
        gmondPollerReference ! PollRequest(pollCounter + 1)
      }
    }
  }
}
