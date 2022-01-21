package com.github.lolgab.mill.scalablytyped

import mill._
import mill.scalalib._
import mill.scalajslib._

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
    os.pwd / "package.json"
  }

  private def scalablyTypedImportTask = T {
    packageJsonSource()
    val ivyLocal = sys.props.get("ivy.home")
      .map(os.Path(_))
      .getOrElse(os.home / ".ivy2") / "local"

    val deps = scalablyTypedWorker().scalablytypedImport(
      os.pwd.toNIO,
      ivyLocal.toNIO,
      scalaVersion(),
      scalaJSVersion()
    )
    deps.map { dep =>
      Dep
        .apply(
          org = dep.groupId,
          name = dep.artifactId,
          version = dep.version,
          cross = CrossVersion.empty(platformed = false) // it comes already platformed
        )
    }

  }

  override def ivyDeps: T[Agg[Dep]] = T {
    super.ivyDeps() ++ scalablyTypedImportTask()
  }
}
