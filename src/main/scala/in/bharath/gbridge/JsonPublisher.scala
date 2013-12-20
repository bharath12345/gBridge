package in.bharath.gbridge

import spray.json.JsObject
import akka.actor.Actor
import akka.event.LoggingReceive

/**
 * Created by bharadwaj on 20/12/13.
 */
object JsonPublisher {
  case class JsonData(jsonObject: JsObject)
}

class JsonPublisher extends Actor {
  import JsonPublisher._
  
  def receive = LoggingReceive {
    case JsonData(jsonObject: JsObject) =>
  }
}
