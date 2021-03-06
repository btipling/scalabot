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
  case class Perform (
    action: String,
    target: String,
    message: String
  )
  case class Network (
    name: String,
    nick: String,
    ssl: Boolean,
    altNick: String,
    adminPassword: String,
    servers: Array[Server],
    channels: Array[String],
    performs: Array[Perform]
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

  def jsBoolean(data: argonaut.JsonObject, key: String) : Boolean = {
    data(key).get.bool.get
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
      Pretty.red(s"Invalid JSON config $configResult")
      System.exit(1)
    }
    val configJson = configResult.toOption.get
    val prettyJson = configJson.spaces2
    Pretty.blue(s"Using config: $prettyJson")
    val config = configJson.obj.get
    val adminPassword = jsString(config, "adminPassword")
    val networksData = config.toMap("networks").array.get
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

      val performsData = networkData("performs").get.array.get
      var performs : List[Perform] = List()
      for (performData <- performsData) {
        val pobj : argonaut.JsonObject = performData.obj.get
        performs ::= new Perform(
          action = pobj("action").get.string.get,
          target = pobj("target").get.string.get,
          message = pobj("message").get.string.get
        )
      }

      networks ::= new Network(
        name = jsString(networkData, "name"),
        nick = jsString(networkData, "nick"),
        altNick = jsString(networkData, "altNick"),
        ssl = jsBoolean(networkData, "ssl"),
        adminPassword = adminPassword,
        servers = servers.toArray,
        channels = jsArray(networkData, "channels"),
        performs = performs.toArray
      )
    }
    new Config(
      configVersion = jsNumber(config, "configVersion"),
      adminPassword = adminPassword,
      networks = networks.toArray
    )
  }
}

