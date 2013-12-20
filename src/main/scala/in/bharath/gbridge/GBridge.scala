package in.bharath.gbridge

import akka.actor.{Props, ActorSystem}
import akka.event.slf4j.SLF4JLogging

/**
 * Created by bharadwaj on 19/12/13.
 */
object Main extends App with Configuration with SLF4JLogging {
  import GmondPoller._

  // Create an Akka system
  val system = ActorSystem("gBridgeSystem")

  // create the result listener, which will print the result and shutdown the system
  val gmondPoller = system.actorOf(Props[GmondPoller], name = "GmondPoller")

  // ToDo: Read the list of gmond's host/port to poll from an external source

  log.debug(s"use file for gmond config = $useFile")

  gmondPoller ! PollRequest("localhost", 8649, 0)
}