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
import akka.actor.Terminated


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
    case Terminated(ircConnection) => handleTermination(ircConnection)
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
      name = "ircListener:" + idGenerator.toString)
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
    context.watch(ircConnection)
  }
  def quitNetwork(quitNet : Listener.QuitNetwork) {
    val id = quitNet.connectionId
    Pretty.yellow("Got a quit from client!")
    val state = connections(id)
    state.connected = false
  }
  def handleTermination(ircConnection : ActorRef) {
    var foundConnection : ConnectionState = null
    var count : Int = 0
    Pretty.red("Handling termination.")
    connections.foreach { keyval => {
      val currentConnection = keyval._2
      if (currentConnection.ircConnection == ircConnection) {
        foundConnection = currentConnection
      }
      count += 1
    }}
    if (foundConnection != null) {
      context stop foundConnection.ircListener
      connections -= foundConnection.id
      if (foundConnection.connected) {
        Pretty.green("Reconnecting...")
        createConnection(foundConnection.networkConfig)
      } else {
        Pretty.red("Staying dead.")
        if (count == 1) {
          Pretty.red("Exiting.")
          context.system.shutdown
        }
      }
    }
  }
}
