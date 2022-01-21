import mill._

import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api.ModuleKind
import $exec.plugins
import com.github.lolgab.mill.scalablytyped._
import $ivy.`com.lihaoyi::utest:0.7.10`
import utest._

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
  val processOutput = os.ProcessOutput.Readlines(lines += _)

  val js = module.fastOpt()
  os.proc("node", js.path).call(stdout = processOutput)
  val out = lines.result().mkString("\n").trim()
  assert(out == "[ [ 1, 3 ], [ 2, 4 ] ]")
  ()
}
