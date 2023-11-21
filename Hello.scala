import scala.scalajs.js
import java.lang.Throwable

object Hello {
  def main(args: Array[String]): Unit = {
    val console = js.Dynamic.global.console
    val msg = "Hello World from Scala.js"
    console.log(msg)
    try {
        throw new Error("test")
    } catch {
        case e: Throwable =>
            println(e)
    }
  }
}
