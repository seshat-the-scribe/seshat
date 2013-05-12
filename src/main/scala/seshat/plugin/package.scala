package seshat

import akka.actor.{ActorLogging, Actor}


/**
 * This package contains the machinery to resolve plugins configs to plugins and ways to instantiate them.
 */
package object plugin {

  case class Plugins(
    inputs:   Set[PluginDescriptor],
    filters:  Set[PluginDescriptor],
    outputs:  Set[PluginDescriptor]
  )

  abstract class Plugin extends Actor with ActorLogging

  case class  PluginDescriptor(
    name:   String,
    clazz:  Class[Plugin]
  )

  case class PluginConfig(
    name:   String,
    config: Map[String,String]
  )


  def resolvePlugins: Plugins = ???

}

