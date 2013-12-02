package scalabot

package floobits

import scalabot.pretty.Pretty

import akka.actor.Actor

class Controller extends Actor {
  def receive = {
    case _ => println("Wtf")
  }
}
