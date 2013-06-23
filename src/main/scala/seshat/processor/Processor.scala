package seshat.processor

import seshat._

import seshat.plugin._
import seshat.plugin.Plugins
import seshat.plugin.PluginDescriptor
import seshat.plugin.PluginConfig


import akka.actor._
import scala.concurrent.Future
import scala.concurrent.duration._

import scala.util.{Failure, Success}



/**
 * Companion Module of the [[seshat.processor.Processor]] class
 *
 * The [[seshat.processor.Processor.Msg]] object defines the msg protocol.
 *
 */
object Processor {

  object Msg {
    case object   Start // Tell inputs to start.
    case object   Stop  // Tell every one to stop
  }

  object Common {
    /** Used to ask for more events  */
    case object GetEvents
    /** This message forces the sending of a GetEvents message to the input */
    case class AskAgain(who: ActorRef, what: Any)
    /** Used to reply to GetEvents */
    case class Events(events: Seq[Event])
    /** Who's queue is full, notify downstream. */
    case class Choked(who:ActorRef)
  }

}

/**
 * Controls the creation of inputs, filters and outputs and acts as supervisor for the
 * whole thing.
 */
class Processor( val config: SeshatConfig, val plugins: Plugins )
  extends Actor
     with ActorLogging {

  import Processor.Msg
  import context.{actorOf,watch}

  def receive: Receive = {
    case Msg.Start  => start()
    case Msg.Stop   => stop()
  }

  def start() {
    inputHandler ! Processor.Msg.Start
    // send to the rest of the handlers
  }
  def stop()  {}

  val inputHandler = watch(actorOf(
    Props(classOf[InputHandler], config, plugins.inputs), "INPUT_HANDLER")
  )

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      log.error(e, "A Child Failed")
      context.stop(self)
      // FIXME Whoever created the processor should watch it
      //       and stop the system if appropriate.
      context.system.shutdown()
      SupervisorStrategy.Escalate
  }

}









