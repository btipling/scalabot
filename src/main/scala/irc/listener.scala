package scalabot

package irc

import scalabot.pretty.Pretty
import scalabot.config

import akka.actor.Actor
import akka.actor.ActorRef
import akka.util.ByteString

object Listener {
  case class QuitNetwork(val connectionId : Int) {}
  case class FlooSubscribe(
    val flooSubscribeMessage : Message.FlooSubscribeMessage,
    val connectionId : Int
  ) {}
}


class Listener extends Actor {
  private var connection : ActorRef = _
  private var leftOver : String = ""
  private var networkConfig : config.Config.Network = _
  private val networkState : NetworkState = new NetworkState()
  private var connectionId : Int = -1
  def receive = {
    case "failed" => Pretty.red("IRC connection failed.")
    case buffer : ByteString => handleBuffer(buffer.utf8String)
    case conf : config.Config.Network => networkConfig = conf
    case id : Int => connectionId = id
  }
  def quit () {
    context.parent ! new Listener.QuitNetwork(connectionId)
  }
  def handleBuffer (buffer: String) {
    if (buffer.isEmpty) {
      return
    }
    val hasLeftOver = buffer.last != '\n'
    val path = sender.path.toString
    if (connection == null && path.contains("ircConnection")) {
      connection = sender
      Pretty.cyan("IRC listener listening.")
    }
    val lines = buffer.split("\n")
    if (hasLeftOver) {
      handleNextLine(lines, 0, lines.length - 1)
      leftOver = lines.last
    } else {
      handleNextLine(lines, 0, lines.length)
    }
  }
  def handleNextLine(lines : Array[String], index : Int, length : Int) {
    if (length == index) {
      return
    }
    val line = lines(index)
    if (leftOver.isEmpty) {
      processLine(line)
    } else {
      processLine(leftOver + line)
      leftOver = ""
    }
    handleNextLine(lines, index + 1, length)
  }
  def processLine(rawLine : String) {
    // Removes \r
    val line = rawLine.substring(0, rawLine.length - 1)
    Pretty.blue(line)
    Handler.handle(line, connection, networkConfig, networkState) match {
      case flooSubscribeMessage : Message.FlooSubscribeMessage => {
        context.parent ! new Listener.FlooSubscribe(
          flooSubscribeMessage = flooSubscribeMessage,
          connectionId = connectionId
        )
      }
      case _ : Message.QuitMessage => quit()
      case _ => {}
    }
  }
}
