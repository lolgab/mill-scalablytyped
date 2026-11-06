import scala.scalajs.js

@main
def main() =
  // `Array.prototype.at` is declared in `lib.es2022.array.d.ts` only
  val array = js.Array(1, 2, 3).asInstanceOf[typings.std.Array[Int]]
  println(array.at(-1))
