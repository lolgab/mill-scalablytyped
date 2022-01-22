package com.github.lolgab.mill.scalablytyped

import mill._
import mill.scalajslib._
import mill.scalalib._

trait ScalablyTyped extends ScalaJSModule {
  // Using `resolveDeps` from `CoursierModule` incorrectly resolves
  // scala-library 2.13 instead of the required 2.12
  private def scalablytypedWorkerClasspath: T[Agg[os.Path]] = T {
    Lib
      .resolveDependencies(
        repositoriesTask(),
        resolveCoursierDependency().apply(_),
        Agg(
          ivy"com.github.lolgab:mill-scalablytyped-worker_2.12:${ScalablyTypedBuildInfo.publishVersion}"
            .exclude("com.github.lolgab" -> "mill-scalablytyped-worker-api")
        ),
        ctx = Some(T.log)
      )
      .map(_.map(_.path))
  }

  private def scalablyTypedWorker = T.task {
    val classpath = scalablytypedWorkerClasspath()
    ScalablyTypedWorkerApi.scalablyTypedWorker().impl(classpath)
  }

  private def packageJsonSource = T.source {
    scalablyTypedBasedPath() / "package.json"
  }

  /** The base path where package.json and node_modules are.
    */
  def scalablyTypedBasePath: T[os.Path] = T { T.workspace }

  /** The typescript dependencies to ignore during the conversion
    */
  def scalablyTypedIgnoredLibs: T[Seq[String]] = T { Seq.empty[String] }

  private def scalablyTypedImportTask = T {
    packageJsonSource()
    val ivyLocal = sys.props
      .get("ivy.home")
      .map(os.Path(_))
      .getOrElse(os.home / ".ivy2") / "local"

    val targetPath = T.dest / "out"

    val deps = scalablyTypedWorker().scalablytypedImport(
      scalablyTypedBasePath().toNIO,
      ivyLocal.toNIO,
      targetPath.toNIO,
      scalaVersion(),
      scalaJSVersion(),
      scalablyTypedIgnoredLibs().toArray
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
