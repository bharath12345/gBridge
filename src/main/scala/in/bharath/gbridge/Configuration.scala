package in.bharath.gbridge

import com.typesafe.config.ConfigFactory
import util.Try
import scala.util.Properties
import java.net.URI

/**
 * Holds service configuration settings.
 */
trait Configuration {
  
  /**
   * Application config object from src/main/resources/application.conf
   */
  val config = ConfigFactory.load()

  lazy val useFile = Try(config.getString("gbridge.useFile")).getOrElse(false)

}
