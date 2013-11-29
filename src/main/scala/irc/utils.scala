package scalabot

package irc

import scalabot.pretty.Pretty

import akka.actor.ActorRef
import akka.util.ByteString

object Utils {

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

  def joinChannelMessage (channel : String) : OutgoingMessage = {
    new OutgoingMessage("JOIN", channel)
  }

  def joinChannelsMessage (channels : Array[String]) : OutgoingMessage = {
    new OutgoingMessage("JOIN", channels.mkString(","))
  }

  def send(sender: ActorRef, message : OutgoingMessage) {
    Pretty.cyan(message.toString)
    sender ! message.toByteString
  }
}
