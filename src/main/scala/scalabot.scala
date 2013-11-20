package Scalabot

import scalaz._, Scalaz._
import argonaut._, Argonaut._

object Scalabot extends App {
  println("Hello World")

  val requestJson  =
    """
    {
      "userid": "1"
    }
    """.stripMargin

  val updatedJson: Option[Json] = for {
    parsed <- requestJson.parseOption
  } yield ("name", jString("testuser")) ->: parsed

  val obj = updatedJson.get.obj
  printf("Updated user: %s\n", updatedJson.toString())
  printf("obj : %s\n", obj.toString())
  printf("userid: %s\n", obj.get.toMap("userid"))
  println("Finished")
}


