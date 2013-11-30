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
    val ircConnection : ActorRef,
    val ircListener : ActorRef,
    val networkConfig : config.Config.Network,
    var connected : Boolean,
    val id : Int) {
  }
  def receive = {
    case network : config.Config.Network => setupNetwork(network)
    case quitNet : Listener.QuitNetwork => quitNetwork(quitNet)
    case _ => Pretty.yellow("Controller got something unexpected.")
  }
  def setupNetwork(network : config.Config.Network) {
    val name = network.name
    Pretty.blue(s"Setting up network: $name")
    val (ircListener, ircConnection) = createConnection(network)
    bindConnection(ircListener, ircConnection, network)
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
  def bindConnection(ircListener : ActorRef, ircConnection : ActorRef,
      conf : config.Config.Network) {
    val id = idGenerator
    idGenerator += 1
    connections(id) = new ConnectionState(
      ircListener = ircListener,
      ircConnection = ircConnection,
      networkConfig = conf,
      connected = true,
      id = id
    )
    ircListener ! id
  }
  def quitNetwork(quitNet : Listener.QuitNetwork) {
    val id = quitNet.connectionId
    Pretty.yellow("Got a quit message!")
    val state = connections(id)
    state.connected = false
    state.ircConnection ! "close"
  }
}
