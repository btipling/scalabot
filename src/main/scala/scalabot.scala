package scalabot

import scalabot.config.Config
import scalabot.pretty.Pretty
import scalabot.irc.connection
import scalabot.irc.listener
import java.net.InetSocketAddress
import akka.actor.ActorSystem
import akka.actor.Props

object Scalabot extends App {
  val config = Config.fetch
  Pretty.green(s"Got config: $config")
  Pretty.red("Finished")
  printf("connection.Client %s\n", connection.Client)
  val address = new InetSocketAddress("chat.freenode.net", 6667)
  Pretty.blue(s"Connecting to $address")
  val system = ActorSystem("IRC")
  val irclistener = system.actorOf(Props[listener.IRCListener], name = "irclistener")
  Pretty.blue(s"listener is $irclistener")
  val conn = system.actorOf(connection.Client.props(address, irclistener))
  Pretty.blue(s"conn is $conn")
}

