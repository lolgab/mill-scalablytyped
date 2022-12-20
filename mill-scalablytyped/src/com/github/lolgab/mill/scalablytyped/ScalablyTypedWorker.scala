package com.github.lolgab.mill.scalablytyped

import com.github.lolgab.mill.scalablytyped.worker.api._
import mill._
import mill.define.Discover
import mill.define.Worker

class ScalablyTypedWorker {
  private var scalaInstanceCache = Option.empty[(Long, ScalablyTypedWorkerApi)]

  def impl(
      scalablytypedWorkerClasspath: Agg[PathRef]
  )(implicit ctx: mill.api.Ctx.Home): ScalablyTypedWorkerApi = {
    val classloaderSig = scalablytypedWorkerClasspath.hashCode
    scalaInstanceCache match {
      case Some((sig, bridge)) if sig == classloaderSig => bridge
      case _ =>
        val cl = mill.api.ClassLoader.create(
          scalablytypedWorkerClasspath.iterator
            .map(_.path.toIO.toURI.toURL)
            .to(Seq),
          parent = null,
          sharedLoader = getClass.getClassLoader,
          sharedPrefixes =
            Seq("com.github.lolgab.mill.scalablytyped.worker.api."),
          logger = None
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

object ScalablyTypedWorkerApi extends mill.define.ExternalModule {
  def scalablyTypedWorker: Worker[ScalablyTypedWorker] = T.worker {
    new ScalablyTypedWorker()
  }
  lazy val millDiscover = Discover[this.type]
}
