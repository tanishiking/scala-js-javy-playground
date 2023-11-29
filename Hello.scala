import scala.scalajs.js
import java.lang.Throwable

object Hello:
  def main(args: Array[String]): Unit =
    val console = js.Dynamic.global.console
    try
      throw new Error("test")
    catch
      case e: Throwable => console.log(e.getMessage)