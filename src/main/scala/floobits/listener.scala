package scalabot

package floobits

import scalabot.pretty.Pretty

import akka.actor.Actor

class Listener extends Actor {
  def receive = {
    case _ => Pretty.yellow("Got something")
  }
}


