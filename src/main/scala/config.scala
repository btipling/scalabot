package scalabot

package config

import scalaz._, Scalaz._
import argonaut._, Argonaut._
import scala.io.Source

object Config {
  def fetch = {
    val source = Source.fromURL(getClass.getResource("/config.json"))
    printf("Hello config %s\n", source.toString())
    val rawConfig = source.mkString
    printf("Well configsz %s\n", rawConfig)
    "Sup"
  }
}
