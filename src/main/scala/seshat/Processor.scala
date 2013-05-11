package seshat

import akka.actor.{Actor, ActorLogging}


/**
 * Companion Module of the [[seshat.Processor]] class
 *
 * The [[seshat.Processor.Msg]] object defines the msg protocol.
 *
 */
object Processor {

  object Msg {
    case object Prepare // Create all the things! (resolve plugins, create children)
    case object Start   // Tell inputs to start.
  }

}

/**
 *
 * Controls the creation of inputs, filters and outputs and acts as controller and hub for the
 * whole thing.
 */
class Processor( val config: SeshatConfig ) extends Actor with ActorLogging {

  import Processor.Msg

  def receive: Receive = {
    case Msg.Prepare  => prepare()
    case Msg.Start    => start()
  }

  def prepare() {}

  def start() {}

}
