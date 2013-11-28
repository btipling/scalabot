package scalabot

package irc

package listener

import akka.actor.Actor
import akka.util.ByteString
import scalabot.pretty.Pretty


class IRCListener extends Actor {
  def receive = {
    case "failed" => Pretty.red("IRC connection failed.")
    case "ByteString" => Pretty.green("Got a bytestring or something")
    case wtf:ByteString => {
      val yo = wtf.utf8String
      Pretty.green(s"got: $yo")
    }
  }
}
