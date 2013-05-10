package seshat

import akka.actor.{Actor, ActorLogging}


/**
 * Companion Module of the [[seshat.Coordinator]] class
 *
 * The [[seshat.Coordinator.Msg]] object defines the msg protocol.
 *
 */
object Coordinator {

  object Msg {
    case object Start
  }

}

/**
 * Controls the creation of inputs, filters and outputs and makes
 */
class Coordinator( val config: SeshatConfig ) extends Actor with ActorLogging {

  import Coordinator.Msg

  def receive: Receive = {
    case Msg.Start =>
  }

}
