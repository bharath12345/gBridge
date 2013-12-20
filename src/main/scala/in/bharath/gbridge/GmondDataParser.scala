package in.bharath.gbridge

import akka.actor.{Props, Actor}
import scala.xml.{XML, Node}
import akka.event.LoggingReceive
import spray.json.{JsString, JsObject}
import in.bharath.gbridge.JsonPublisher.JsonData

/**
 * Created by bharadwaj on 20/12/13.
 */
object GmondDataParser {
  case class DataXml(lines: List[String])
}

class GmondDataParser extends Actor {
  import GmondDataParser._

  def attributeEquals(name: String, value: String)(node: Node) = {
    node.attribute(name).exists(n => n.text == value)
  }

  def gcd(a: Int, b: Int): Int = if (b == 0) a.abs else gcd(b, a % b)

  def lcm(a: Int, b: Int) = (a * b).abs / gcd(a, b)

  val jsonPublisher = context.actorOf(Props[JsonPublisher], name = "JsonPublisher")

  def receive = LoggingReceive {

    case DataXml(lines) => {

      val relevant = lines.dropWhile(line => line.contains("<GANGLIA_XML") == false)

      //for(line <- relevant) println(s"line ==> $line")

      val singleBlob = relevant.flatten.mkString
      //println(singleBlob)

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
              host     : "hostName",
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

      val clusterXML = (xml \\ "CLUSTER").filter(attributeEquals("NAME", "unspecified"))
      //println(s"cluster = $clusterXML")

      val nodeXML = (clusterXML \\ "HOST").filter(attributeEquals("NAME", "192.168.1.113")) //192.168.1.113 or 10.50.1.235
      //println(s"node = $nodeXML")

      val metricTuples = (nodeXML \ "METRIC").map(x => ((x \ "@NAME").text, (x \ "@TMAX").text.toInt))
      println("metric tuples = " + metricTuples)

      val periods: List[Int] = metricTuples.map(t => t._2).toList
      val periodGCD = periods.foldLeft(0)(gcd(_, _))
      println(s"gcd of periods = $periodGCD")

      val pollingCycles = metricTuples.map(t => (t._1, t._2 / periodGCD))
      println(s"poll cycles = $pollingCycles")

      val metrics = metricTuples.map(t => t._1)
      for (m <- metrics) {
        val metricXML = (nodeXML \ "METRIC").filter(attributeEquals("NAME", m))
        //println(s"metric values = " + metricValue)

        val singleMetric = JsObject(
          "cluster" -> JsString("unspecified"),
          "host" -> JsString("localhost"),
          "ip" -> JsString("192.168.1.113"),
          "instance" -> JsString(""),
          "metric" -> JsString(m),
          "value" -> JsString((metricXML \ "@VAL").text),
          "type" -> JsString((metricXML \ "@TYPE").text),
          "units" -> JsString((metricXML \ "@UNITS").text),
          "period" -> JsString((metricXML \ "@TMAX").text)
        )

        println(s"json = " + singleMetric)

        jsonPublisher ! JsonData(singleMetric)
      }
    }
  }
}
