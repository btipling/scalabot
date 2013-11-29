package scalabot

package irc

import scalabot.config
import scalabot.pretty.Pretty

import akka.actor.ActorRef
import akka.util.ByteString

object Handler {
  class OutgoingMessage (action : String, key : String, value : String = "") {
    override def toString : String = {
      if (value.isEmpty) {
        s"$action $key \r\n"
      } else {
        s"$action $key : $value \r\n"
      }
    }
    def toByteString : ByteString = {
      ByteString(this.toString)
    }
  }
  def handle(line : String, sender: ActorRef, networkConfig : config.Config.Network,
    networkState : NetworkState) {
    if (line.isEmpty) {
      return
    }
    if (!networkState.connected) {
      send(sender, new OutgoingMessage("NICK", networkConfig.nick))
      send(sender, new OutgoingMessage("User", "scalabot 8 *", "scalabot"))
      networkState.connected = true
    }
    val m = Message.getMessage(line)
    m match {
      case ping : Message.PingMessage => {
        send(sender, new OutgoingMessage("PONG", ping.host))
      }
      case _ => {}
    }
  }
  def send(sender: ActorRef, message : OutgoingMessage) {
    Pretty.cyan(message.toString)
    sender ! message.toByteString
  }
}
