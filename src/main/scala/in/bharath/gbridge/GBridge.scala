package in.bharath.gbridge

import akka.actor.{Props, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import spray.json._
import spray.json.DefaultJsonProtocol._

/**
 * Created by bharadwaj on 19/12/13.
 */
object Main extends App with Configuration with SLF4JLogging {

  import GmondPoller._

  case class HostInfo(hostIP: String, hostName: String, port: Int)
  case class ClusterInfo(clusterName: String, hostInfo: List[HostInfo])
  case class GangliaInfo(gangliaConf: List[ClusterInfo])

  object GBridgeJsonProtocol extends DefaultJsonProtocol {
    implicit val hostInfo = jsonFormat3(HostInfo)
    implicit val clusterInfo = jsonFormat2(ClusterInfo)
  }

  import GBridgeJsonProtocol._

  implicit object GangliaInfoFormat extends RootJsonFormat[GangliaInfo] {
    def write(b: GangliaInfo) = JsObject()
    def read(value: JsValue) = GangliaInfo(value.convertTo[List[ClusterInfo]])
  }

  // Create an Akka system
  val system = ActorSystem("gBridgeSystem")

  // ToDo: Read the list of gmond's host/port to poll from an external source
  if (useFile) log.debug(s"using gmond config file = $clusterConfig")
  else         log.debug("not using gmond config file")

  if(useFile) {
    val source = scala.io.Source.fromFile(clusterConfig)
    val lines: String = source.mkString
    source.close()

    //println("json config = " + lines)
    val jsonAst = lines.asJson
    val config: GangliaInfo = jsonAst.convertTo[GangliaInfo]
    println("the json object = " + config.toString)

    for {
      ci <- config.gangliaConf
      hi <- ci.hostInfo
    } {

      val pollerName = new StringBuilder("GmondPoller_").append(ci.clusterName).append("_").
        append(hi.hostName).append("_").
        append(hi.hostIP).append("_").append(hi.port).toString()

      //println(s"poller name = " + pollerName)
      // create the result listener, which will print the result and shutdown the system
      val gmondPoller = system.actorOf(Props(new GmondPoller(ci.clusterName, hi.hostIP, hi.hostName, hi.port)), name = pollerName)

      /*
       Send the request to GmondPoller Actor => The reading data from gmond socket happens in a future.
       So, the actor is NOT blocked consuming a resource like a thread.
       Not consuming resources like threads is what makes asynchronous programming useful.
       In this case the, the ExecutionContext takes care of waking up the future when the response is ready, creating the
       dormant objects in its context and using one of its threads. It frees up threading resources for other compute
       intensive tasks - no computing resource like thred is ever blocked.
      */
      gmondPoller ! PollRequest(0L)
    }
  }
}