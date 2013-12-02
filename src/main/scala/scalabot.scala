package scalabot

import scalabot.config.Config
import scalabot.pretty.Pretty
import scalabot.irc.Controller

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props

object Scalabot extends App {
  val config = Config.fetch
  val networks = config.networks
  val customConf = ConfigFactory.parseString("""
    akka {
      loglevel = Debug
    }
  """)
  val ircSystem = ActorSystem("IRC", ConfigFactory.load(customConf))
  val ircController = ircSystem.actorOf(Props[Controller],
    name = "ircController")
  for (network <- config.networks) {
    ircController ! network
  }
  Pretty.cyan("Finished setting up")
}

