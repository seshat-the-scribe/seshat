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

  implicit val timeout = Timeout(2 seconds)

  val system = ActorSystem("Seshat")

  sys addShutdownHook {
    system.shutdown()
  }


  def main(args: Array[String]) {

    try {

      val name = args(0)
      start(name,system)

    } catch {

      case e: Exception =>
        system.shutdown()
        println((e.getMessage))
        log.error( (e.getMessage) )
        throw e

    }

  }


  def failBadNumberFormatWith(msg: String) =
    catching(classOf[NumberFormatException]).withApply( x => throw RTX(msg + " => " + x.getMessage) )

  def failExceptionWith(msg: String) =
    catching(classOf[Exception]).withApply( x => throw RTX(msg + " => " + x.getMessage) )

  def failNoSuchElementWith(msg: String) =
    catching(classOf[NoSuchElementException]).withApply( x => throw RTX(msg + " => " + x.getMessage) )


  private lazy val log = LoggerFactory.getLogger(getClass)

}


