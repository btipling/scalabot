package scalabot

package irc

import scalabot.config
import scalabot.pretty.Pretty

import akka.actor.ActorRef

object Handler {
  def handle(line : String, sender: ActorRef, networkConfig : config.Config.Network,
    networkState : NetworkState) {
    Pretty.cyan("Handling a line $line")
  }
}
