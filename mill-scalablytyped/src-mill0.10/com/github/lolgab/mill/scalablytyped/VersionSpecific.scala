package com.github.lolgab.mill.scalablytyped

import mill._
import mill.scalajslib._
import mill.scalalib._

private[scalablytyped] trait VersionSpecific extends ScalaJSModule {
  // Using `resolveDeps` from `CoursierModule` incorrectly resolves
  // scala-library 2.13 instead of the required 2.12
  private[scalablytyped] def scalablytypedWorkerClasspath: T[Agg[PathRef]] = T {
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
  }
}
