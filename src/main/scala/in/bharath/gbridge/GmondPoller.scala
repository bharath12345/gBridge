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

  case class PollRequest(host: String, port: Int, pollCounter: Int)

  case class PollingCycle(cluster: String, host: String, tupleList: Seq[(String, Int)], pollCounter: Int, port: Int)

}

class GmondPoller extends Actor with SLF4JLogging {

  import GmondPoller._
  import GmondDataParser._
  import ExecutionContext.Implicits.global

  val gmondDataParser = context.actorOf(Props[GmondDataParser], name = "GmondDataParser")

  def receive = LoggingReceive {
    case PollRequest(host, port, pollCounter) =>  {

      val adr = InetAddress.getByName(host)
      val socket = new Socket(adr, port)

      log.debug("received poll request for host = " + host)
      //val commander = sender

      future {
        Thread.sleep(3000)
        log.debug("going to poll host = " + host)

        // ToDo: this is a blocking receive - ELIMINATE it or else it will bring down the performance of this whole actor

        val s = Source.fromInputStream(socket.getInputStream())

        val lines = (for (line <- s.getLines()) yield line).toList

        socket.close()
        s.close()

        lines

      } onSuccess {
        case lines => {
          //log.debug("x = " + x)
          gmondDataParser ! DataXml(lines, pollCounter, port)
        }
      }
    }

    case PollingCycle(cluster, host, tupleList, pollCounter, port) => {
      // use the tupleList to send a message to self for the next polling

      log.debug("received polling request")

      // ToDo: This is the dangerous scale
      //self ! PollRequest(host, port, pollCounter)
    }
  }
}
