package in.bharath.gbridge

import spray.json._
import java.net.{Socket, InetAddress}
import scala.io._
import scala.xml.{XML, Node}
import akka.actor.{Actor, Props, ActorSystem}
import akka.event.LoggingReceive

/**
 * Created by bharadwaj on 19/12/13.
 */
object Main extends App with Configuration {
  import GmondPoller._

  // Create an Akka system
  val system = ActorSystem("gBridgeSystem")

  // create the result listener, which will print the result and shutdown the system
  val gmondPoller = system.actorOf(Props[GmondPoller], name = "GmondPoller")

  // ToDo: Read the list of gmond's host/port to poll from an external source

  println(s"use file for gmond config = $useFile")

  gmondPoller ! PollRequest("localhost", 8649)
}