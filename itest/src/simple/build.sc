import mill._

import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api.ModuleKind
import $exec.plugins
import com.github.lolgab.mill.scalablytyped._
import $ivy.`com.lihaoyi::utest:0.8.1`
import utest._

object module extends ScalaJSModule with ScalablyTyped {
  def scalaVersion = "2.13.10"
  def scalaJSVersion = "1.11.0"
  def moduleKind = ModuleKind.CommonJSModule
}

def verify() = T.command {
  val lines = Seq.newBuilder[String]
  val processOutput = os.ProcessOutput.Readlines(lines += _)

  val js = module.fastOpt()
  os.proc("node", js.path).call(stdout = processOutput)
  val out = lines.result().mkString("\n").trim()
  assert(out == "[ [ 1, 3 ], [ 2, 4 ] ]")
  ()
}
