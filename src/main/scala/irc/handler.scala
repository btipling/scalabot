package scalabot

package irc

import scalabot.config
import scalabot.pretty.Pretty

import akka.actor.ActorRef

object Handler {
  def handle(line : String, sender: ActorRef, networkConfig : config.Config.Network,
    networkState : NetworkState) : Message.IncomingMessage = {
    if (line.isEmpty) {
      return null
    }
    if (!networkState.connected) {
      Utils.send(sender, new Utils.OutgoingMessage("NICK", networkConfig.nick))
      Utils.send(sender, new Utils.OutgoingMessage("User", "scalabot 8 *", "scalabot"))
      networkState.connected = true
      networkState.nick = networkConfig.nick
    }
    val m = Message.getMessage(line, networkState.nick)
    m match {
      case ping : Message.PingMessage => {
        Utils.send(sender, new Utils.OutgoingMessage("PONG", ping.host))
        ping
      }
      case serverMessage: Message.ServerMessage => {
        if (networkState.joined) {
          return serverMessage
        }
        if (serverMessage.id < 10) {
          return serverMessage
        }
        if (!networkConfig.channels.isEmpty) {
          Utils.send(sender, Utils.joinChannelsMessage(networkConfig.channels))
        }
        for (p <- networkConfig.performs) {
          Utils.send(sender, new Utils.OutgoingMessage(p.action, p.target, p.message))
        }
        networkState.joined = true
        serverMessage
      }
      case privateMessage: Message.PrivateMessage => {
        privateMessage
      }
      case quitMessage: Message.QuitMessage => {
        if (quitMessage.validate(networkConfig)) {
          return quitMessage
        }
        null
      }
      case _ => {
        null
      }
    }
  }
}
