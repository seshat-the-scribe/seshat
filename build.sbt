import AssemblyKeys._ // put this at the top of the file

name          := "seshat"

version       := "0.0.1-SNAPSHOT"

organization  := "L3"

scalaVersion  := "2.10.1"

seq(assemblySettings: _*)

mainClass in (Compile, run) := Some( "seshat.SeshatMain" )

jarName in assembly <<= (name, version) map ( (n,v) => "seshat-"+v+"-full.jar" )

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    //case x if x startsWith  "javax/servlet"   => MergeStrategy.last
    case x if x endsWith "about.html" => MergeStrategy.discard
    case x => old(x)
  }
}


libraryDependencies ++= Seq(
                              "com.typesafe.akka"                   %%  "akka-actor"                % "2.2-M3",
                              "com.typesafe.akka"                   %%  "akka-slf4j"                % "2.2-M3",
                              "ch.qos.logback"                      %   "logback-classic"           % "1.0.9",
                              "ch.qos.logback"                      %   "logback-core"              % "1.0.9",
                              "org.eclipse.jetty"                   %   "jetty-servlet"             % "9.0.1.v20130408",
                              "com.yammer.metrics"                  %   "metrics-core"              % "3.0.0-BETA1",
                              "com.typesafe.akka"                   %%  "akka-testkit"              % "2.2-M3"    % "test",
                              "org.specs2"                          %%  "specs2"                    % "1.12.3"   % "test"   )


testOptions in Test += Tests.Argument( "console", "junitxml" )

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xcheckinit",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-optimise",
  "-feature",
  "-encoding", "utf8"
)

