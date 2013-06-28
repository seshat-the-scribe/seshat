package seshat.plugin.input

import seshat.plugin.{InputPlugin, PluginConfig}
import seshat.Event
import java.util.Date

/** This input plugins exists only for testing purposes.
  *
  * One can send it `Events` or `Any` and it will send them to the parent.
  * `Any`s are previously wrapped in an Event.
  *
  */
class Dummy(config:PluginConfig) extends InputPlugin(config) {

  override def receive = defaultHandler orElse {
    case e: Event => context.parent ! Event
    case a: Any   => context.parent ! buildEvent(a)
  }

  private def buildEvent( a: Any ) =
    Event( a, "DUMMY", new Date().getTime )

}



