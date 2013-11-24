package scalabot

import scalabot.config.Config
import scalabot.pretty.Pretty

object Scalabot extends App {
  val config = Config.fetch
  Pretty.green(s"Got config: $config")
  Pretty.red("Finished")
}


