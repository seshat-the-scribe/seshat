package seshat.config

/**
 * This package contains the machinery to resolve plugins configs to plugins and ways to instantiate them.
 */
package object plugins {

  case class Plugins(
    inputs:   Set[PluginDescriptor[InputPlugin]],
    filters:  Set[PluginDescriptor[FilterPlugin]],
    outputs:  Set[PluginDescriptor[OutputPlugin]]
  )

  trait PluginDescriptor[T <: Plugin] {
    val name: String
    val clazz: Class[T]
  }

  case class PluginConfig(
    name:   String,
    config: Map[String,String]
  )

  trait Plugin

  trait InputPlugin   extends Plugin
  trait FilterPlugin  extends Plugin
  trait OutputPlugin  extends Plugin

}

