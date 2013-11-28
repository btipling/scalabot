package scalabot

package irc

import scalabot.config
import scalabot.pretty.Pretty

import scala.collection.mutable
import java.net.InetSocketAddress
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props


class Controller extends Actor {
  private val connections : mutable.Map[Int, ConnectionState] = mutable.Map.empty[Int, ConnectionState]
  private var idGenerator : Int = 0
  class ConnectionState (
    ircConnection : ActorRef,
    ircListener : ActorRef,
    networkConfig : config.Config.Network,
    id : Int) {
  }
  def receive = {
    case network : config.Config.Network => {
      val name = network.name
      Pretty.blue(s"Setting up network: $name")
      val (ircListener, ircConnection) = createConnection(network)
      bindConnection(ircListener, ircConnection, network)
    }
    case _ => Pretty.yellow("Controller got something unexpected.")
  }
  def createConnection(network : config.Config.Network) = {
    val server = network.servers(0)
    val address = new InetSocketAddress(server.host, server.port)
    Pretty.blue(s"Connecting to $address")
    val ircListener = context.actorOf(Props[Listener],
      name = "ircListener")
    ircListener ! network
    val ircConnection = context.actorOf(Connection.props(address,
      ircListener), name = "ircConnection")
    (ircListener, ircConnection)
  }
  def bindConnection(ircListener : ActorRef, ircConnection : ActorRef, conf : config.Config.Network) {
      connections(idGenerator) = new ConnectionState(
        ircListener = ircListener,
        ircConnection = ircConnection,
        networkConfig = conf,
        id = idGenerator
      )
      idGenerator += 1
  }
}
