import typings.lodash.mod.^
import typings.std.global.console
import scala.scalajs.js

object main extends App {
  val res = ^.partition(js.Array(1, 2, 3, 4), n => n.asInstanceOf[Int] % 2)
  console.log(res)
}
