import sbt._

import sbtassembly.Plugin._
import AssemblyKeys._

object L3Build extends Build
{

  lazy val root =
    Project(
      "root", 
      file("."), 
      settings = Project.defaultSettings ++ assemblySettings ++ AgentBots.settings
    ) 

}

object L3Settings {

  lazy val settings = Seq( bundleTask )

  val bundle     = TaskKey[Unit]("bundle", "Bundles stuff")
  val bundleTask = bundle <<= (assembly) map { _ => Bundler() }

}

