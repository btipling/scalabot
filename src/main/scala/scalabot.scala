package scalabot

import scalabot.config.Config
import scalabot.pretty.Pretty
import scalabot.irc.controller

import akka.actor.ActorSystem
import akka.actor.Props

object Scalabot extends App {
  val config = Config.fetch
  val networks = config.networks
  val ircSystem = ActorSystem("IRC")
  val ircController = ircSystem.actorOf(Props[controller.IRCController],
    name = "ircController")
  for (network <- config.networks) {
    ircController ! network
  }
  Pretty.cyan("Finished setting up")
}

