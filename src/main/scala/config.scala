package scalabot

package config

import scalaz._, Scalaz._
import argonaut._, Argonaut._
import scala.io.Source

object Config {
  case class Network (
    name: String,
    servers: Array[String],
    nick: String,
    altNick: String,
    channels: Array[String],
    performs: Array[String]
  )
  case class Config (
    configVersion: Double,
    adminPassword: String,
    networks: Array[Network]
  )

  def fetch = {
    val source = Source.fromURL(getClass.getResource("/config.json"))
    printf("Hello config %s\n", source.toString())
    val rawConfig = source.mkString
    printf("Well configsz %s\n", rawConfig)
    val configResult = JsonParser.parse(rawConfig)
    if (configResult.isLeft) {
      printf("Invalid JSON config %s\n", configResult.toString)
      System.exit(1)
    }
    val config = configResult.toOption.get.obj.get.toMap
    printf("config thing %s\n", config.toString)
    printf("config version: %s\n", config("configVersion"))
    printf("config networks: %s\n", config("networks").toString)
    printf("type of networks: %s\n", config("networks").getClass.getSimpleName)
    val networks = config("networks").array.get
    printf("type of networks: %s\n", networks.getClass.getSimpleName)
    for (network <- networks) {
      printf("network? : %s\n", network.toString)
    }
    "Sup"
  }
}
