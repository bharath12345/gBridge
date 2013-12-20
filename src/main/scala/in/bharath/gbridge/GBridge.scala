package in.bharath.gbridge

import java.net.{Socket, InetAddress}
import scala.io._
import scala.xml.{XML, Node}

/**
 * Created by bharadwaj on 19/12/13.
 */
class Main {

  def attributeEquals(name: String, value: String)(node: Node) = {
    node.attribute(name).exists(name => name.text == value)
  }

  def work() = {

    // ToDo: Read the list of gmond's host/port to poll from an external source

    val adr = InetAddress.getByName("localhost")
    val socket = new Socket(adr, 8649)

    val s = Source.fromInputStream(socket.getInputStream())

    val lines = (for (line <- s.getLines()) yield line).toList
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
       one way to tackle this problem is to find the LCD of all the polling intervals and poll gmond per the LCM
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
    */


    //val testResults = xml \\ "CLUSTER" filter attributeEquals("NAME","unspecified")

    //val testResults = (xml \\ "CLUSTER").filter(node => node.attribute("NAME").exists(name => name.text == "unspecified"))

    val testResults = (xml \\ "CLUSTER").filter(attributeEquals("NAME", "unspecified"))

    println(s"test results = $testResults")
  }

}

object Main extends App {

  val m = new Main()
  m.work()


}