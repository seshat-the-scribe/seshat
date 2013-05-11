package seshat


import scala.util.control.Exception._
import scala.collection.JavaConverters._
import scala.concurrent.duration._

import org.slf4j.LoggerFactory


import com.typesafe.config._
import java.io.File

import akka.util.Timeout
import akka.actor.ActorSystem
import seshat.config.plugins.Plugins

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
      val plugins = Plugins(Set(),Set(),Set())     //FIXME get a real Plugins instance
      val coord = spawnCoordinator(system, config, plugins )
      coord ! Processor.Msg.Start


    } catch {

      case e: Exception =>
        system.shutdown()
        println((e.getMessage))
        log.error( (e.getMessage) )
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
      input   <- Option(config.getObject("input"))
      filter  <- Option(config.getObject("filters"))
      output  <- Option(config.getObject("output"))
    } yield (
      SeshatConfig(
        name,
        input,
        filter,
        output
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


