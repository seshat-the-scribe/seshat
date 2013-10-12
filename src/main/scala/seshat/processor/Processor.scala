package seshat.processor

import seshat._

import seshat.plugin.Plugins

import akka.actor._


/**
 * Companion Module of the [[seshat.processor.Processor]] class
 *
 * The [[seshat.processor.Processor.Msg]] object defines the msg protocol.
 *
 */
object Processor {

  object Msg {
    case object   Start // Tell inputs to start.
    case object   Stop  // Tell everyone to stop
  }

  object Internal {

    /** Used to ask for more events  */
    case object NextBatch
    /** This message forces the sending of a NextBatch message. */
    case class AskAgain(who: ActorRef, what: Any)
    /** Used to reply to NextBatch */
    case class Batch(events: Seq[Event])
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

    case Msg.Start => start()
    case Msg.Stop  => stop()

    case Terminated(who) =>
      log.error("Terminated child")
      context.system.shutdown()
      context.system.awaitTermination()

  }

  def start() {
    inputHandler  ! Processor.Msg.Start
    filterHandler ! Processor.Msg.Start
    outputHandler ! Processor.Msg.Start
  }
  def stop()  {
    inputHandler  ! Processor.Msg.Stop
    filterHandler ! Processor.Msg.Stop
    outputHandler ! Processor.Msg.Stop
  }

  val inputHandler = watch(actorOf(
    Props(classOf[InputHandler], config, plugins.inputs), "INPUT_HANDLER")
  )

  val filterHandler = watch(actorOf(
    Props(classOf[FilterHandler], inputHandler, config, plugins.filters), "FILTER_HANDLER")
  )

  val outputHandler = watch(actorOf(
    Props(classOf[OutputHandler], filterHandler, config, plugins.outputs), "OUTPUT_HANDLER")
  )

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      log.info(s"A Child Failed with exception ${e.getMessage}")
      context.stop(self)
      // FIXME Whoever created the processor should watch it
      //       and stop the system if appropriate.
      SupervisorStrategy.Escalate
  }

}









