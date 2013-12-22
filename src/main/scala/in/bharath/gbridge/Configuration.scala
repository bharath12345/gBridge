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

  lazy val useFile: Boolean = Try(config.getBoolean("gbridge.useFile")).getOrElse(false)

  lazy val clusterConfig: String = Try(config.getString("gbridge.clusterConfig")).getOrElse("/tmp/clusterConfig.json")

}
