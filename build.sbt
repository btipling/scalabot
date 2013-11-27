import com.typesafe.sbt.SbtStartScript

name := "scalabot"

version := "0.1"

scalaVersion := "2.10.3"

// Dependencies
resolvers += "SonaType" at "https://oss.sonatype.org/content/groups/public"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

seq(SbtStartScript.startScriptForClassesSettings: _*)
