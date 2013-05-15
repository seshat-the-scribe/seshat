package seshat

import collection.JavaConverters._
import com.typesafe.config.{ConfigFactory, ConfigValue}
import java.io.File
import seshat.plugin.PluginConfig
import org.slf4j.LoggerFactory

/**
 *   Utilities to make working with Config objects easier.
 */
package object config {

  /** Enhances a ConfigValue
   *
   * @param value the value to enhance.
   *
   */
  implicit class EnhancedConfigValue(val value: ConfigValue) extends AnyVal {

    import java.util.HashMap

    def toMap = {
      lazy val log = LoggerFactory.getLogger(getClass)
      try {
        value.unwrapped
                  .asInstanceOf[HashMap[String,String]]
                  .asScala
                  .toMap
      } catch {
        // FIXME try to get a single value if it is one.
        case e: ClassCastException => 
          log.error("Cannot convert "+value.unwrapped+" of class "+value.unwrapped.getClass.getName+" to Map ("+value.render+")")          
          throw e
      }

    }
  }

  def buildConfig(name: String) = {

    val file = new File(name)

    val config = ConfigFactory.parseFile(file)


    val inputConfig =
      config.getObject("input").asScala.map( { case (k,v) => PluginConfig(k,v.toMap) }  ).toSet

    val filterConfig =
      config.getObject("filter").asScala.map( { case (k,v) => PluginConfig(k,v.toMap) }  
    ).toSet

    val outputConfig =
      config.getObject("output").asScala.map( { case (k,v) => PluginConfig(k,v.toMap)  }  ).toSet

    SeshatConfig(
      file.getName,
      inputConfig,
      filterConfig,
      outputConfig
    )



  }


}
