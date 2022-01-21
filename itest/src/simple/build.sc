import mill._

import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api.ModuleKind
import $exec.plugins
import com.github.lolgab.mill.scalablytyped._

object module extends ScalaJSModule with ScalablyTyped {
  def scalaVersion = "3.1.0"
  def scalaJSVersion = "1.8.0"
  def moduleKind = ModuleKind.CommonJSModule
}

def prepare() = T.command {
  os.proc("npm", "install").call()
  ()
}

def verify() = T.command {
  val lines = Seq.newBuilder[String]
  val out = os.ProcessOutput.Readlines(lines += _)

  val js = module.fastOpt()
  os.proc("node", js.path).call(stdout = out)
  assert(lines.result() == "[[1,3],[2,4]]")
  ()
}
