package scalabot

package pretty

import scala.Console

object Pretty {
  def pretty(msg: String, fg: String, bg: String, bold: Boolean) {
    if (bold) {
      printf("%s%s%s%s%s\n", Console.BOLD, fg, bg, msg, Console.RESET)
    } else {
      printf("%s%s%s%s\n", fg, bg, msg, Console.RESET)
    }
  }
  def blue(msg: String, bg: String = "", bold: Boolean = false) {
    pretty(msg, Console.BLUE, bg, bold)
  }
  def yellow(msg: String, bg: String = "", bold: Boolean = false) {
    pretty(msg, Console.YELLOW, bg, bold)
  }
  def green(msg: String, bg: String = "", bold: Boolean = false) {
    pretty(msg, Console.GREEN, bg, bold)
  }
  def cyan(msg: String, bg: String = "", bold: Boolean = false) {
    pretty(msg, Console.CYAN, bg, bold)
  }
  def magenta(msg: String, bg: String = "", bold: Boolean = false) {
    pretty(msg, Console.MAGENTA, bg, bold)
  }
  def red(msg: String, bg: String = "", bold: Boolean = false) {
    pretty(msg, Console.RED, bg, bold)
  }
}
