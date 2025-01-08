package com.github.lolgab.mill.scalablytyped

import com.github.lolgab.mill.scalablytyped.worker.api.ScalablyTypedWorkerFlavour
import mill._
import mill.scalajslib._
import mill.scalalib._

trait ScalablyTyped extends ScalaJSModule with VersionSpecific {
  private def scalablyTypedWorker = T.task {
    val classpath = scalablytypedWorkerClasspath()
    ScalablyTypedWorkerApi.scalablyTypedWorker().impl(classpath)
  }

  private def packageJsonSource = T.source {
    scalablyTypedBasePath() / "package.json"
  }

  /** The base path where package.json and node_modules are.
    */
  def scalablyTypedBasePath: T[os.Path] = T { T.workspace }

  /** The TypeScript dependencies to ignore during the conversion
    */
  def scalablyTypedIgnoredLibs: T[Seq[String]] = T { Seq.empty[String] }

  /** When true (which is the default) uses scala-js-dom types when possible
    * instead of types we translate from typescript in std
    */
  def useScalaJsDomTypes: T[Boolean] = T { true }

  /** ScalablyTyped flavours so far enables rich interop with react.
    */
  def scalablyTypedFlavour: T[Flavour] = T {
    Flavour.Normal
  }

  /** Generate facades for dev dependencies as well.
    */
  def scalablyTypedIncludeDev: T[Boolean] = T { false }

  private def scalablyTypedImportTask = T {
    packageJsonSource()
    val ivyLocal = sys.props
      .get("ivy.home")
      .map(os.Path(_))
      .getOrElse(sys.env.get("IVY_HOME")
                 .map(os.Path(_))
                 .getOrElse(os.home / ".ivy2")) / "local"

    val targetPath = T.dest / "out"

    val flavour = scalablyTypedFlavour() match {
      case Flavour.Normal       => ScalablyTypedWorkerFlavour.Normal
      case Flavour.Slinky       => ScalablyTypedWorkerFlavour.Slinky
      case Flavour.SlinkyNative => ScalablyTypedWorkerFlavour.SlinkyNative
      case Flavour.ScalajsReact => ScalablyTypedWorkerFlavour.ScalajsReact
    }

    val deps = scalablyTypedWorker().scalablytypedImport(
      scalablyTypedBasePath().toNIO,
      ivyLocal.toNIO,
      targetPath.toNIO,
      scalaVersion(),
      scalaJSVersion(),
      scalablyTypedIgnoredLibs().toArray,
      useScalaJsDomTypes(),
      scalablyTypedIncludeDev(),
      flavour
    )
    deps.map { dep =>
      Dep
        .apply(
          org = dep.groupId,
          name = dep.artifactId,
          version = dep.version,
          cross = CrossVersion.empty(
            platformed = false // it comes already platformed
          )
        )
    }

  }

  override def ivyDeps: T[Agg[Dep]] = T {
    super.ivyDeps() ++ scalablyTypedImportTask()
  }
}
