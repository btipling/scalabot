package scalabot

package irc

package handler

import scalabot.config
import scalabot.pretty.Pretty
import scalabot.irc.networkstate

import akka.actor.ActorRef

object Handler {
  def handle(line : String, sender: ActorRef, networkConfig : config.Config.Network,
    networkState : networkstate.NetworkState) {
    Pretty.cyan("Handling a line $line")
  }
}
