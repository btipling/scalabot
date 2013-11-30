package scalabot

package irc

import scalabot.config
import scalabot.pretty.Pretty

import akka.actor.ActorRef

object Message {

  val ServerMessageExpression = "^:(\\S+) (\\d+) (\\S+) :([\\S| ]*)$".r
  val PingExpression = "^PING :(\\S+)".r
  val PrivateExpression = "^(\\S+)!(\\S+) (\\S+) (\\S+) :([\\S| ]*)$".r

  def getMessage(line : String, myNick : String) : IncomingMessage = {
    line match {
      case ServerMessageExpression(host, id, target, message) => {
        new ServerMessage(host, id.toInt, target, message)
      }
      case PingExpression(host) => {
        new PingMessage(host)
      }
      case PrivateExpression(fromNick, fromHost, action, target, message) => {
        new PrivateMessage(
          myNick = myNick,
          fromNick = fromNick,
          fromHost = fromHost,
          action = action,
          target = target,
          message = message
        ).parse
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

  case class PrivateMessage(
    myNick: String,
    fromNick: String,
    fromHost: String,
    action: String,
    target: String,
    message: String
  ) extends IncomingMessage {
    val nickExpression = (s"$myNick: ([\\S| ]*)" + "$").r
    val quitExpression = "quit (\\S+)$".r
    val joinExpression = "join (\\S+) (\\S+)$".r
    val partExpression = "part (\\S+) (\\S+)$".r
    def parse () : IncomingMessage = {
      var checkMessage = message
      if (target != myNick) {
        message match {
          case nickExpression(newMessage) => {
            checkMessage = newMessage
          }
          case _ => {
            return this
          }
        }
      }
      checkMessage match {
        case quitExpression(password) => {
          new QuitMessage(password)
        }
        case joinExpression(channel, password) => {
          new JoinMessage(channel, password)
        }
        case partExpression(channel, password) => {
          new PartMessage(channel, password)
        }
        case _ => {
          this
        }
      }
    }
  }

  case class AdminMessage(
    password : String
  ) extends IncomingMessage {
    def validate (networkConfig : config.Config.Network) : Boolean = {
      val adminPassword = networkConfig.adminPassword
      password == adminPassword
    }
    protected def perform_ (sender : ActorRef) {
    }
    def perform (sender : ActorRef) : IncomingMessage = {
      perform_(sender)
      this
    }
  }

  class QuitMessage(
    password : String
  ) extends AdminMessage(password) {
    override def perform_ (sender : ActorRef) = {
      Utils.send(sender, new Utils.OutgoingMessage("QUIT", "", "Scalabot"))
    }
  }

  class JoinMessage(
    channel: String,
    password : String
  ) extends AdminMessage(password) {
    override def perform_ (sender : ActorRef) {
      Pretty.yellow(s"joining $channel")
      Utils.send(sender, new Utils.OutgoingMessage("JOIN", channel))
    }
  }

  class PartMessage(
    channel: String,
    password : String
  ) extends AdminMessage(password) {
    override def perform_ (sender : ActorRef) {
      Pretty.yellow(s"parting $channel")
      Utils.send(sender, new Utils.OutgoingMessage("PART", channel))
    }
  }

}
