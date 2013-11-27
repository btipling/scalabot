package scalabot

package irc

package listener

import akka.actor.Actor
import scalabot.pretty.Pretty


class IRCListener extends Actor {
  def receive = {
    case "failed" => Pretty.red("IRC connection failed.")
    case "ByteString" => Pretty.green("Got a bytestring or something")
    case _ => Pretty.yellow("Nfi what just happened")
  }
}
