package scalabot

package irc

package connection

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress


object Client {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Client], remote, replies)
}

class Client(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import Tcp._
  implicit val system = context.system


  val manager = IO(Tcp)

  manager ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      println("Connection failed")
      listener ! "failed"
      context stop self

    case c @ Connected(remote, local) =>
      println("Connection connected")
      listener ! c
      val connection = sender
      connection ! Register(self)
      context become {
        case data: ByteString => connection ! Write(data)
        case CommandFailed(w: Write) => // O/S buffer was full
        case Received(data) => {
          println("received some stuff")
          listener ! data
        }
        case "close" => connection ! Close
        case _: ConnectionClosed => context stop self
      }
  }
}

