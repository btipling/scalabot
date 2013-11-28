package scalabot

package irc

import scalabot.pretty.Pretty

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress


object Connection {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Connection], remote, replies)
}

class Connection(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import Tcp._
  implicit val system = context.system

  val manager = IO(Tcp)

  manager ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      val host = remote.toString
      Pretty.red(s"Connection to $host failed.")
      listener ! "failed"
      context stop self

    case c @ Connected(remote, local) =>
      val host = remote.toString
      Pretty.green(s"Connected to $host.")
      listener ! c
      val connection = sender
      connection ! Register(self)
      context become {
        case data: ByteString => connection ! Write(data)
        case CommandFailed(w: Write) => Pretty.red(s"Write to $host failed")
        case Received(data) => {
          listener ! data
        }
        case "close" => {
          Pretty.yellow(s"Closed conection to $host.")
          connection ! Close
        }
        case _: ConnectionClosed => {
          Pretty.yellow(s"Closed conection to $host.")
          context stop self
        }
      }
  }
}

