package in.bharath.gbridge

import spray.json.JsObject
import akka.actor.Actor
import akka.event.LoggingReceive
import akka.event.slf4j.SLF4JLogging
import org.zeromq.ZMQ

/**
 * Created by bharadwaj on 20/12/13.
 */
object JsonPublisher {
  case class JsonData(jsonObject: JsObject)
}

class JsonPublisher extends Actor with SLF4JLogging {
  import JsonPublisher._

  def receive = LoggingReceive {
    case JsonData(jsonObject: JsObject) => {
      val context = ZMQ.context(1)
      val socket = context.socket(ZMQ.REQ)
      socket.connect ("tcp://localhost:5555")
      val requestBytes = jsonObject.toString().getBytes
      socket.send(requestBytes, 0)
    }
  }
}
