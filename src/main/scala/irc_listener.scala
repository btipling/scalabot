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
      Pretty.yellow(s"Nfi what just happened $wtf")
      val wtf2: ByteString = wtf
      val yo = wtf2.utf8String
      Pretty.green(s"got: $yo")
    }
  }
}
