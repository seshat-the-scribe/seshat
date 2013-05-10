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
    case object Prepare // Create all the things! (resolve plugins, create children)
    case object Start   // Tell inputs to start.
  }

}

/**
 *
 * Controls the creation of inputs, filters and outputs and acts as controller and hub for the
 * whole thing.
 *
 * {{{
 *
 * TODO resolve plugins and instantiate as appropriate.
 * TODO create output children
 * TODO create filter pipeline passing outputs
 * TODO create children inputs passing pipeline
 * TODO start inputs
 * TODO receive events from inputs and pipeTo filter pipeline
 * TODO receive events from filter pipeline and pipeTo outputs
 *
 * TODO measure all the things!
 *
 * }}}
 *
 */
class Coordinator( val config: SeshatConfig ) extends Actor with ActorLogging {

  import Coordinator.Msg.Start

  def receive: Receive = {
    case Start =>
  }

}
