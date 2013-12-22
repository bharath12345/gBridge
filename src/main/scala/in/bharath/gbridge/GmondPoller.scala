package in.bharath.gbridge

import akka.actor.{Props, Actor}
import akka.event.LoggingReceive
import java.net.{Socket, InetAddress}
import scala.io.Source
import akka.event.slf4j.SLF4JLogging
import scala.concurrent._

/**
 * Created by bharadwaj on 20/12/13.
 */
object GmondPoller {
  case class PollRequest(pollCounter: Long)
}

class GmondPoller(clusterName: String, hostIP: String, hostName: String, port: Int) extends Actor with SLF4JLogging {

  import GmondPoller._
  import GmondDataParser._
  import ExecutionContext.Implicits.global

  val parserName = new StringBuilder("GmondParser_").append(clusterName).append("_").
    append(hostName).append("_").
    append(hostIP).append("_").append(port).toString()

  val gmondDataParser = context.actorOf(Props(new GmondDataParser(clusterName, hostIP, hostName, port)), name = parserName)

  def receive = LoggingReceive {
    case PollRequest(pollCounter) =>  {

      val adr = InetAddress.getByName(hostName)
      val socket = new Socket(adr, port)
      log.debug(s"poll request for [clusterName = $clusterName] [hostName = $hostName] [hostIP = $hostIP] " +
        s"[port = $port] [poll counter = $pollCounter]")

      future {
        //Thread.sleep(3000)
        //log.debug(s"going to poll hostIP = $hostIP")

        val s = Source.fromInputStream(socket.getInputStream())
        val lines = (for (line <- s.getLines()) yield line).toList
        socket.close()
        s.close()

        lines

      } onSuccess {
        case lines => {
          //println("lines size = " + lines.size)
          if(lines.size > 0) gmondDataParser ! DataXml(pollCounter, lines)
        }
      }
    }
  }
}
