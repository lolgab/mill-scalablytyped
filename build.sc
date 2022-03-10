import mill._

import mill.scalalib._
import mill.scalalib.api.Util.scalaNativeBinaryVersion
import mill.scalalib.publish._
import $ivy.`com.lihaoyi::mill-contrib-buildinfo:$MILL_VERSION`
import mill.contrib.buildinfo.BuildInfo
import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.4.1-16-63f11c`
import de.tobiasroeser.mill.integrationtest._
import $ivy.`com.goyeau::mill-scalafix::0.2.8`
import com.goyeau.mill.scalafix.ScalafixModule
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.1.1`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import os.Path

val millVersions = Seq("0.10.0")
val millBinaryVersions = millVersions.map(scalaNativeBinaryVersion)

def millBinaryVersion(millVersion: String) = scalaNativeBinaryVersion(
  millVersion
)
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
    extends Cross[MillScalablyTypedCross](millBinaryVersions: _*)
class MillScalablyTypedCross(millBinaryVersion: String)
    extends ScalaModule
    with CommonPublish
    with BuildInfo
    with ScalafixModule {
  override def millSourcePath = super.millSourcePath / os.up
  override def artifactName = s"mill-scalablytyped_mill$millBinaryVersion"

  override def sources = T.sources(
    super.sources() ++ Seq(millSourcePath / s"src-mill$millBinaryVersion")
      .map(PathRef(_))
  )
  def scalaVersion = "2.13.8"
  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-scalajslib:${millVersion(millBinaryVersion)}"
  )
  def moduleDeps = Seq(`mill-scalablytyped-worker-api`)

  def ivyDeps = super.ivyDeps() ++ Agg()

  def buildInfoMembers = Map(
    "publishVersion" -> publishVersion()
  )
  def buildInfoObjectName = "ScalablyTypedBuildInfo"
  def buildInfoPackageName = Some("com.github.lolgab.mill.scalablytyped")

  def scalacOptions =
    super.scalacOptions() ++ Seq("-Ywarn-unused", "-deprecation")

  def scalafixIvyDeps = Agg(ivy"com.github.liancheng::organize-imports:0.4.4")
}

object `mill-scalablytyped-worker-api` extends JavaModule with CommonPublish

object `mill-scalablytyped-worker` extends ScalaModule with CommonPublish {
  def moduleDeps = Seq(`mill-scalablytyped-worker-api`)
  def scalaVersion = "2.12.12"
  def ivyDeps = Agg(
    ivy"org.scalablytyped.converter::importer:1.0.0-beta37",
    ivy"org.apache.logging.log4j:log4j-core:2.17.2"
  )
}

object itest extends Cross[itestCross]("0.10.0")
class itestCross(millVersion: String) extends MillIntegrationTestModule {
  override def millSourcePath: Path = super.millSourcePath / os.up
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
