package in.bharath.gbridge

import akka.actor.{Props, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import akka.pattern.{ask, pipe}
import in.bharath.gbridge.GmondDataParser.DataXml
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

/**
 * Created by bharadwaj on 19/12/13.
 */
object Main extends App with Configuration with SLF4JLogging {
  import GmondPoller._

  implicit val timeout = Timeout(5.seconds)

  // Create an Akka system
  val system = ActorSystem("gBridgeSystem")

  // create the result listener, which will print the result and shutdown the system
  val gmondPoller = system.actorOf(Props[GmondPoller], name = "GmondPoller")
  //val gmondDataParser = system.actorOf(Props[GmondDataParser], name = "GmondDataParser")

  // ToDo: Read the list of gmond's host/port to poll from an external source
  log.debug(s"use file for gmond config = $useFile")

  /*
   Send the request to GmondPoller Actor => The return will be a Future.
   So, the return will come, whenever it will come. The actor's is NOT blocked consuming a resource like a thread.
   Not consuming resources like threads is what makes asynchronous programming useful.
   In this case the, the ExecutionContext takes care of waking up the future when the response is ready, creating the
   dormant objects in its context and using one of its threads. It frees up threading resources for other compute
   intensive tasks - no computing resource like thred is ever blocked.
  */
  (gmondPoller ? PollRequest("localhost", 8649, 0))//.mapTo[DataXml]) pipeTo gmondDataParser

  (gmondPoller ? PollRequest("localhost", 8649, 0))//.mapTo[DataXml]) pipeTo gmondDataParser

  (gmondPoller ? PollRequest("localhost", 8649, 0))//.mapTo[DataXml]) pipeTo gmondDataParser

  (gmondPoller ? PollRequest("localhost", 8649, 0))//.mapTo[DataXml]) pipeTo gmondDataParser


}