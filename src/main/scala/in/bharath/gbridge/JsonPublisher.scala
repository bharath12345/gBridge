package in.bharath.gbridge

import spray.json.JsObject
import akka.actor.Actor
import akka.event.LoggingReceive
import akka.event.slf4j.SLF4JLogging

/**
 * Created by bharadwaj on 20/12/13.
 */
object JsonPublisher {
  case class JsonData(jsonObject: JsObject)
}

class JsonPublisher extends Actor with SLF4JLogging {
  import JsonPublisher._

  def receive = LoggingReceive {
    case JsonData(jsonObject: JsObject) =>
  }
}
