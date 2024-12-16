import mill._

import mill.scalalib._
import mill.scalalib.publish._
import $ivy.`com.lihaoyi::mill-contrib-buildinfo:`
import mill.contrib.buildinfo.BuildInfo
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.7.1`
import de.tobiasroeser.mill.integrationtest._
import $ivy.`com.goyeau::mill-scalafix::0.4.2`
import com.goyeau.mill.scalafix.ScalafixModule
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import os.Path

def millBinaryVersion(millVersion: String) = millVersion match {
  case s"0.12.$_" => s"0.11"
  case s"0.$m.$p" => s"0.$m"
}
val millVersions = Seq("0.10.0", "0.11.0")
val millBinaryVersions = millVersions.map(millBinaryVersion)

val scala212 = "2.12.18"

def millVersion(binaryVersion: String) =
  millVersions.find(v => millBinaryVersion(v) == binaryVersion).get

trait CommonPublish extends PublishModule {
  def pomSettings = PomSettings(
    description = "ScalablyTyped Mill Plugin",
    organization = "com.github.lolgab",
    url = "https://github.com/lolgab/mill-scalablytyped",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("lolgab", "mill-scalablytyped"),
    developers = Seq(
      Developer("lolgab", "Lorenzo Gabriele", "https://github.com/lolgab")
    )
  )
  def publishVersion = VcsVersion.vcsState().format()
}

object `mill-scalablytyped`
    extends Cross[MillScalablyTypedCross](millBinaryVersions)
trait MillScalablyTypedCross
    extends ScalaModule
    with CommonPublish
    with BuildInfo
    with ScalafixModule
    with Cross.Module[String] {
  def millBinaryVersion: String = crossValue
  override def artifactName = s"mill-scalablytyped_mill$millBinaryVersion"

  override def sources = T.sources(
    super.sources() ++ Seq(
      millSourcePath / s"src-mill${millVersion(millBinaryVersion).split('.').take(2).mkString(".")}"
    )
      .map(PathRef(_))
  )
  def scalaVersion = "2.13.10"
  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-scalajslib:${millVersion(millBinaryVersion)}"
  )
  def moduleDeps = Seq(`mill-scalablytyped-worker-api`)

  def ivyDeps = super.ivyDeps() ++ Agg()

  def buildInfoMembers = Seq(
    BuildInfo.Value("publishVersion", publishVersion()),
    BuildInfo.Value("scala212Version", scala212)
  )
  def buildInfoObjectName = "ScalablyTypedBuildInfo"
  def buildInfoPackageName = "com.github.lolgab.mill.scalablytyped"

  def scalacOptions =
    super.scalacOptions() ++ Seq("-Ywarn-unused", "-deprecation")

  def scalafixIvyDeps = Agg(ivy"com.github.liancheng::organize-imports:0.6.0")
}

object `mill-scalablytyped-worker-api` extends JavaModule with CommonPublish

object `mill-scalablytyped-worker` extends ScalaModule with CommonPublish {
  def moduleDeps = Seq(`mill-scalablytyped-worker-api`)
  def scalaVersion = scala212
  def ivyDeps = Agg(
    ivy"org.scalablytyped.converter::importer:1.0.0-beta44",
    ivy"org.apache.logging.log4j:log4j-core:2.17.2"
  )
}

object itest extends Cross[itestCross]("0.10.0", "0.10.12", "0.11.0", "0.12.3")
trait itestCross extends MillIntegrationTestModule with Cross.Module[String] {
  def millVersion = crossValue
  def millTestVersion = millVersion
  def pluginsUnderTest = Seq(
    `mill-scalablytyped`(millBinaryVersion(millVersion))
  )
  def temporaryIvyModules = Seq(
    `mill-scalablytyped-worker`,
    `mill-scalablytyped-worker-api`
  )
  def testBase = millSourcePath / "src"
  def testInvocations = T {
    Seq(
      PathRef(testBase / "simple") -> Seq(
        TestInvocation.Targets(Seq("prepare")),
        TestInvocation.Targets(Seq("verify"))
      )
    )
  }
}
