package com.github.lolgab.mill.scalablytyped

import mill._
import mill.scalajslib._
import mill.scalalib._

private[scalablytyped] trait VersionSpecific extends ScalaJSModule {
  private[scalablytyped] def scalablytypedWorkerClasspath: T[Agg[PathRef]] = T {
    Lib
      .resolveDependencies(
        repositoriesTask(),
        Agg(
          ivy"com.github.lolgab::mill-scalablytyped-worker:${ScalablyTypedBuildInfo.publishVersion}"
            .exclude("com.github.lolgab" -> "mill-scalablytyped-worker-api")
        ).map(Lib.depToBoundDep(_, ScalablyTypedBuildInfo.scala212Version)),
        ctx = Some(T.log)
      )
  }
}
