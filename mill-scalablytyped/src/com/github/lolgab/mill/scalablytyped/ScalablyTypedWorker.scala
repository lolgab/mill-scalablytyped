package com.github.lolgab.mill.scalablytyped

import com.github.lolgab.mill.scalablytyped.worker.api._
import mill._
import mill.api.Discover
import mill.api.TaskCtx

class ScalablyTypedWorker {
  private var scalaInstanceCache = Option.empty[(Long, ScalablyTypedWorkerApi)]

  def impl(
      scalablytypedWorkerClasspath: Seq[PathRef]
  ): ScalablyTypedWorkerApi = {
    val classloaderSig = scalablytypedWorkerClasspath.hashCode
    scalaInstanceCache match {
      case Some((sig, bridge)) if sig == classloaderSig => bridge
      case _ =>
        val cl = mill.util.Jvm.createClassLoader(
          scalablytypedWorkerClasspath.map(_.path),
          parent = null,
          sharedLoader = getClass.getClassLoader,
          sharedPrefixes =
            Seq("com.github.lolgab.mill.scalablytyped.worker.api.")
        )
        try {
          val bridge = cl
            .loadClass(
              "com.github.lolgab.mill.scalablytyped.worker.ScalablyTypedWorkerImpl"
            )
            .getDeclaredConstructor()
            .newInstance()
            .asInstanceOf[
              com.github.lolgab.mill.scalablytyped.worker.api.ScalablyTypedWorkerApi
            ]
          scalaInstanceCache = Some((classloaderSig, bridge))
          bridge
        } catch {
          case e: Exception =>
            e.printStackTrace()
            throw e
        }
    }
  }
}

object ScalablyTypedWorkerApi extends mill.api.ExternalModule {
  def scalablyTypedWorker: Worker[ScalablyTypedWorker] = Task.Worker {
    new ScalablyTypedWorker()
  }
  lazy val millDiscover = Discover[this.type]
}
