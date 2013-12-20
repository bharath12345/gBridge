package in.bharath.gbridge

import akka.actor.{Props, Actor}
import akka.event.LoggingReceive
import java.net.{Socket, InetAddress}
import scala.io.Source

/**
 * Created by bharadwaj on 20/12/13.
 */
object GmondPoller {
  case class PollRequest(host: String, port: Int)
}

class GmondPoller extends Actor {
  import GmondPoller._
  import GmondDataParser._

  val gmondDataParser = context.actorOf(Props[GmondDataParser], name = "GmondDataParser")

  def receive = LoggingReceive {
    case PollRequest(host, port) => {
      val adr = InetAddress.getByName(host)
      val socket = new Socket(adr, port)
      val s = Source.fromInputStream(socket.getInputStream())

      val lines = (for (line <- s.getLines()) yield line).toList

      socket.close()
      s.close()

      gmondDataParser ! DataXml(lines)
    }
  }
}
