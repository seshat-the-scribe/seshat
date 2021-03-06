package seshat


import scala.util.control.Exception._
import scala.concurrent.duration._

import org.slf4j.LoggerFactory

import akka.util.Timeout
import akka.actor.ActorSystem
import seshat.config._

/**
 *
 * User: f
 * Date: 11/04/13
 * Time: 17:21
 */
object SeshatMain {

  val system = ActorSystem("Seshat")

  sys.addShutdownHook {
    log.info("Shutting down system")
    system.shutdown()
  }


  def main(args: Array[String]) {

    try {

      val name = args(0)
      log.info("Starting with name")
      start(name, system)

    } catch {

      case e: Exception =>
        system.shutdown()
        log.error( (e.getMessage) )
        e.printStackTrace()
        sys.exit(1)

    }

  }

  private lazy val log = LoggerFactory.getLogger(getClass)

}


