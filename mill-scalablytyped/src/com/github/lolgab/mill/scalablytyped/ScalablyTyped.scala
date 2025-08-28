package com.github.lolgab.mill.scalablytyped

import com.github.lolgab.mill.scalablytyped.worker.api.ScalablyTypedWorkerFlavour
import mill._
import mill.api.CrossVersion
import mill.scalajslib._
import mill.scalalib._

trait ScalablyTyped extends ScalaJSModule {
  private def scalablyTypedWorker = Task.Anon {
    val classpath = scalablytypedWorkerClasspath()
    ScalablyTypedWorkerApi.scalablyTypedWorker().impl(classpath)
  }

  private def packageJsonSource = Task.Anon {
    Task.Source {
      scalablyTypedBasePath() / "package.json"
      // Task.workspace / "package.json"
    }
  }

  /** The base path where package.json and node_modules are.
    */
  def scalablyTypedBasePath: T[os.Path] = Task {
    os.Path(sys.env("MILL_WORKSPACE_ROOT"))
  }

  /** The TypeScript dependencies to ignore during the conversion
    */
  def scalablyTypedIgnoredLibs: T[Seq[String]] = Task { Seq.empty[String] }

  /** When true (which is the default) uses scala-js-dom types when possible
    * instead of types we translate from typescript in std
    */
  def useScalaJsDomTypes: T[Boolean] = Task { true }

  /** ScalablyTyped flavours so far enables rich interop with react.
    */
  def scalablyTypedFlavour: T[Flavour] = Task {
    Flavour.Normal
  }

  /** The top-level package to put generated code in.
    */
  def scalablyTypedOutputPackage: T[String] = Task {
    scalablyTypedWorker().defaultOutputPackage()
  }

  /** Generate facades for dev dependencies as well.
    */
  def scalablyTypedIncludeDev: T[Boolean] = Task { false }

  private def scalablyTypedImportTask = Task {
    packageJsonSource()
    val ivyLocal = sys.props
      .get("ivy.home")
      .map(os.Path(_))
      .getOrElse(os.home / ".ivy2") / "local"

    val targetPath = Task.dest / "out"

    val flavour = scalablyTypedFlavour() match {
      case Flavour.Normal       => ScalablyTypedWorkerFlavour.Normal
      case Flavour.Slinky       => ScalablyTypedWorkerFlavour.Slinky
      case Flavour.SlinkyNative => ScalablyTypedWorkerFlavour.SlinkyNative
      case Flavour.ScalajsReact => ScalablyTypedWorkerFlavour.ScalajsReact
    }

    val basePath = scalablyTypedBasePath()

    val deps =
      scalablyTypedWorker().scalablytypedImport(
        basePath.toNIO,
        ivyLocal.toNIO,
        targetPath.toNIO,
        scalaVersion(),
        scalaJSVersion(),
        scalablyTypedIgnoredLibs().toArray,
        useScalaJsDomTypes(),
        scalablyTypedIncludeDev(),
        flavour,
        scalablyTypedOutputPackage()
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

  private def scalablytypedWorkerClasspath: T[Seq[PathRef]] =
    Task {
      Lib
        .resolveDependencies(
          repositoriesTask(),
          Seq(
            mvn"com.github.lolgab::mill-scalablytyped-worker:${ScalablyTypedBuildInfo.publishVersion}"
              .exclude("com.github.lolgab" -> "mill-scalablytyped-worker-api")
          ).map(Lib.depToBoundDep(_, ScalablyTypedBuildInfo.scala212Version)),
          ctx = Some(Task.ctx()),
          checkGradleModules = false
        )
    }

  override def mvnDeps: T[Seq[Dep]] = Task {
    super.mvnDeps() ++ scalablyTypedImportTask()
  }
}
