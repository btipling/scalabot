package scalabot

package irc

package controller

import scalabot.config
import scalabot.pretty.Pretty
import scalabot.irc.connection
import scalabot.irc.listener

import scala.collection.mutable
import java.net.InetSocketAddress
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

class ConnectionState (
  ircConnection : ActorRef,
  ircListener : ActorRef,
  id : Int) {
}

class IRCController extends Actor {
  private val connections : mutable.Map[Int, ConnectionState] = mutable.Map.empty[Int, ConnectionState]
  private var idGenerator : Int = 0
  def receive = {
    case network : config.Config.Network => {
      val name = network.name
      Pretty.blue(s"Setting up network: $name")
      val (ircListener, ircConnection) = createConnection(network)
      bindConnection(ircListener, ircConnection)
    }
    case _ => Pretty.yellow("IRCController got something unexpected.")
  }
  def createConnection(network : config.Config.Network) = {
    val server = network.servers(0)
    val address = new InetSocketAddress(server.host, server.port)
    Pretty.blue(s"Connecting to $address")
    val ircListener = context.actorOf(Props[listener.IRCListener],
      name = "ircListener")
    val ircConnection = context.actorOf(connection.IRCConnection.props(address,
      ircListener), name = "ircConnection")
    (ircListener, ircConnection)
  }
  def bindConnection(ircListener : ActorRef, ircConnection : ActorRef) {
      connections(idGenerator) = new ConnectionState(
        ircListener = ircListener,
        ircConnection = ircConnection,
        id = idGenerator
      )
      idGenerator += 1
  }
}
