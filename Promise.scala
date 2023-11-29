import scala.concurrent._
import scala.util.Success
import concurrent.ExecutionContext.Implicits.global

object Promise:
  def fetchData(): Future[String] = Future { "some data!" }
  def main(args: Array[String]): Unit =
    val f = fetchData()
    f.onComplete:
      case Success(data) => println(data)
