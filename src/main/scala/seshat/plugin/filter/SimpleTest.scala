package seshat.plugin.filter

import seshat.plugin.{PluginConfig, FilterPlugin}
import seshat.Event
import java.util.Date

/**
 * Created by f on 6/26/13.
 */
class SimpleTest(config: PluginConfig) extends FilterPlugin(config)  {

  def apply(event:Event): Event = {
    event.copy(fields = event.fields + ( "FilteredAt"->(new Date().toString) ))
  }

}
