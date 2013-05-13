package seshat


/**
 * This package contains the machinery to resolve plugin configs to plugins and ways to instantiate them.
 *
 */
package object plugin {

  case class Event(
    raw: Array[Byte],
    kind: String,
    timestamp: Long,
    originalTimeStamp: Option[Long],
    field: Map[String, String] = Map(),
    tags: Set[String] = Set()
  )

  case class Plugins(
    inputs:   Set[PluginDescriptor],
    filters:  Set[PluginDescriptor],
    outputs:  Set[PluginDescriptor]
  )

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

