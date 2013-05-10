package seshat


import scala.util.control.Exception._
import scala.collection.JavaConverters._
import scala.concurrent.duration._

import org.slf4j.LoggerFactory


import com.typesafe.config._
import java.io.File

import akka.util.Timeout
import akka.actor.ActorSystem


import icc.util.arguments._
import icc.util.AnsiColors._

/**
 *
 * User: f
 * Date: 11/04/13
 * Time: 17:21
 */
object SeshatMain {

  implicit val timeout = Timeout(2 seconds)

  val system = ActorSystem("Seshat")

  sys addShutdownHook {
    system.shutdown()
  }


  def main(args: Array[String]) {

    try {

      val name = args(0)

      val config = buildConfig( name )

      log.info( s"Launching coordinator with config $config" )
      val coord = spawnCoordinator(system, config)
      coord ! Processor.Msg.Start


    } catch {

      case e: Exception =>
        system.shutdown()
        println(Red(e.getMessage))
        log.error( Red(e.getMessage) )
        throw e

    }

  }


  // TODO move to a config package
  //      with all config and plugin loading machinery.
  // TODO load plugins by reading plugins.conf
  //      and merging it with builtinPlugins.conf
  def buildConfig(name: String) = {

    val config = ConfigFactory.parseFile(new File(name))

    log.debug( "building configses"  )

    (for {
      cfg      <- Option(config)
      inputs   <- Option(cfg.getConfigList("inputs").asScala.toSeq)
      filters  <- Option(cfg.getConfigList("filters").asScala.toSeq)
      outputs  <- Option(cfg.getConfigList("outputs").asScala.toSeq)
    } yield (
      SeshatConfig(
        name,
        inputs,
        filters,
        outputs
      )
    )).head

  }



  def failBadNumberFormatWith(msg: String) =
    catching(classOf[NumberFormatException]).withApply( x => throw RTX(msg + " => " + x.getMessage) )

  def failExceptionWith(msg: String) =
    catching(classOf[Exception]).withApply( x => throw RTX(msg + " => " + x.getMessage) )

  def failNoSuchElementWith(msg: String) =
    catching(classOf[NoSuchElementException]).withApply( x => throw RTX(msg + " => " + x.getMessage) )


  private lazy val log = LoggerFactory.getLogger(getClass)

}


