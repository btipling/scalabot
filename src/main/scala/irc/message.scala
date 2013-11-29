package scalabot

package irc

import scalabot.pretty.Pretty

object Message {

  val ServerMessageExpression = "^:(\\S+) (\\d+) (\\S+) :([\\S| ]*)$".r
  val PingExpression = "^PING :(\\S+)".r

  def getMessage(line : String) : IncomingMessage = {
    line match {
      case ServerMessageExpression(host, id, target, message) => {
        new ServerMessage(host, id.toInt, target, message)
      }
      case PingExpression(host) => {
        new PingMessage(host)
      }
      case _ => {
        new IgnoredMessage()
      }
    }
  }

  class IncomingMessage {}

  case class IgnoredMessage() extends IncomingMessage {}

  case class ServerMessage(
    host : String,
    id : Int,
    target: String,
    message : String = ""
  ) extends IncomingMessage {}

  case class PingMessage(
    host : String,
    Message : String = ""
  ) extends IncomingMessage {}

}
