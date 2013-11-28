package scalabot

package config

import scalaz._, Scalaz._
import argonaut._, Argonaut._
import scala.io.Source
import scalabot.pretty.Pretty

object Config {
  case class Server (
    host: String,
    port: Int
  )
  case class Network (
    name: String,
    nick: String,
    altNick: String,
    servers: Array[Server],
    channels: Array[String],
    performs: Array[String]
  )
  case class Config (
    configVersion: Double,
    adminPassword: String,
    networks: Array[Network]
  )

  def jsNumber(data: argonaut.JsonObject, key: String) : Double = {
    data(key).get.number.get
  }

  def jsString(data: argonaut.JsonObject, key: String) : String = {
    data(key).get.string.get
  }

  def jsArray(data: argonaut.JsonObject, key: String) : Array[String] = {
    val subData = data(key).get.array.get
    subData.map(x => x.string.get).toArray
  }

  def fetch : Config = {
    val source = Source.fromURL(getClass.getResource("/config.json"))
    val rawConfig = source.mkString
    val configResult = JsonParser.parse(rawConfig)
    if (configResult.isLeft) {
      printf("Invalid JSON config %s\n", configResult.toString)
      System.exit(1)
    }
    val configJson = configResult.toOption.get
    val prettyJson = configJson.spaces2
    Pretty.blue(s"Using config: $prettyJson")
    val config = configJson.obj.get
    val networksData = config.toMap("networks").array.get
    Pretty.red(s"wtf: $networksData")
    var networks : List[Network] = List()
    for (data <- networksData) {
      val networkData = data.obj.get
      val serversData = networkData("servers").get.array.get
      var servers : List[Server] = List()
      for (serverData <- serversData) {
        val sobj : argonaut.JsonObject = serverData.obj.get
        servers ::= new Server(
          host = sobj("host").get.string.get,
          port = sobj("port").get.number.get.toInt
        )
      }
      networks ::= new Network(
        name = jsString(networkData, "name"),
        nick = jsString(networkData, "nick"),
        servers = servers.toArray,
        altNick = jsString(networkData, "altNick"),
        channels = jsArray(networkData, "channels"),
        performs = jsArray(networkData, "performs")
      )
    }
    new Config(
      configVersion = jsNumber(config, "configVersion"),
      adminPassword = jsString(config, "adminPassword"),
      networks = networks.toArray
    )
  }
}

