package scalabot

package irc

import scalabot.config
import scalabot.pretty.Pretty

import akka.actor.ActorRef
import akka.util.ByteString

object Handler {
  class Message (action : String, key : String, value : String = "") {
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
    if (!networkState.connected) {
      send(sender, new Message("NICK", networkConfig.nick))
      send(sender, new Message("User", "scalabot 8 *", "scalabot"))
      networkState.connected = true
    }
  }
  def send(sender: ActorRef, message : Message) {
    Pretty.cyan(message.toString)
    sender ! message.toByteString
  }
}
