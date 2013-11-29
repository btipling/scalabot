package scalabot

package irc

import scalabot.config
import scalabot.pretty.Pretty

import akka.actor.ActorRef

object Handler {
  def handle(line : String, sender: ActorRef, networkConfig : config.Config.Network,
    networkState : NetworkState) {
    if (line.isEmpty) {
      return
    }
    if (!networkState.connected) {
      Utils.send(sender, new Utils.OutgoingMessage("NICK", networkConfig.nick))
      Utils.send(sender, new Utils.OutgoingMessage("User", "scalabot 8 *", "scalabot"))
      networkState.connected = true
    }
    val m = Message.getMessage(line)
    m match {
      case ping : Message.PingMessage => {
        Utils.send(sender, new Utils.OutgoingMessage("PONG", ping.host))
      }
      case serverMessage: Message.ServerMessage => {
        if (networkState.joined) {
          return
        }
        if (serverMessage.id < 10) {
          return
        }
        if (!networkConfig.channels.isEmpty) {
          Utils.send(sender, Utils.joinChannelsMessage(networkConfig.channels))
        }
        for (p <- networkConfig.performs) {
          Utils.send(sender, new Utils.OutgoingMessage(p.action, p.target, p.message))
        }
        networkState.joined = true
      }
      case _ => {}
    }
  }
}
