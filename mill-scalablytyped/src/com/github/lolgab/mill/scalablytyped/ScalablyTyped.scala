package com.github.lolgab.mill.scalablytyped

import com.github.lolgab.mill.scalablytyped.worker.api.ScalablyTypedWorkerFlavour
import mill._
import mill.scalajslib._
import mill.scalalib._

trait ScalablyTyped extends ScalaJSModule {
  private def scalablyTypedWorker = Task.Anon {
    val classpath = scalablytypedWorkerClasspath()
    ScalablyTypedWorkerApi.scalablyTypedWorker().impl(classpath)
  }

  /** The path of package.json. When overriding you need to override also
    * `scalablyTypedBasePath` accordingly.
    */
  def scalablyTypedPackageJson: T[PathRef] = Task.Source {
    mill.api.BuildCtx.workspaceRoot / "package.json"
  }

  /** All the libs to run ScalablyTyped on */
  def scalablyTypedWantedLibs = Task {
    val packageJson = scalablyTypedPackageJson()
    val includeDev = scalablyTypedIncludeDev()
    val includePeer = scalablyTypedIncludePeer()
    val ignoredLibs = scalablyTypedIgnoredLibs().toSet

    val json =
      upickle.default.read[PackageJson](os.read.stream(packageJson.path))

    def when(cond: Boolean)(map: collection.MapView[String, String]) =
      if cond then map else collection.MapView.empty

    val baseLibs = when(includePeer)(json.peerDependencies.view) ++
      when(includeDev)(json.devDependencies.view) ++
      json.dependencies.view

    baseLibs.filter((key, _) => !ignoredLibs.contains(key)).toMap
  }

  /** The base path where package.json and node_modules are. When overriding you
    * need to override also `scalablyTypedPackageJson` accordingly.
    */
  def scalablyTypedBasePath: T[os.Path] = Task {
    mill.api.BuildCtx.workspaceRoot
  }

  /** The TypeScript dependencies to ignore during the conversion
    */
  def scalablyTypedIgnoredLibs: T[Seq[String]] = Task { Seq.empty[String] }

  /** When true (which is the default) uses scala-js-dom types when possible
    * instead of types we translate from typescript in std
    */
  def scalablyTypedUseScalaJsDomTypes: T[Boolean] = Task {
    true
  }

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

  /** Generate facades for peer dependencies as well.
    */
  def scalablyTypedIncludePeer: T[Boolean] = Task { false }

  private def scalablyTypedImportTask = Task {
    val ivyLocal = sys.props
      .get("ivy.home")
      .map(os.Path(_))
      .getOrElse(os.home / ".ivy2") / "local"

    val targetPath = Task.dest / "src"

    val flavour = scalablyTypedFlavour() match {
      case Flavour.Normal => ScalablyTypedWorkerFlavour.Normal
      case Flavour.Slinky => ScalablyTypedWorkerFlavour.Slinky
      case Flavour.SlinkyNative => ScalablyTypedWorkerFlavour.SlinkyNative
      case Flavour.ScalajsReact => ScalablyTypedWorkerFlavour.ScalajsReact
    }

    val basePath = scalablyTypedBasePath()

    scalablyTypedWorker()
      .scalablytypedImport(
        basePath.toNIO,
        ivyLocal.toNIO,
        scalaVersion(),
        scalablyTypedWantedLibs().keys.toArray,
        scalablyTypedIgnoredLibs().toArray,
        scalablyTypedUseScalaJsDomTypes(),
        scalablyTypedIncludeDev(),
        flavour,
        scalablyTypedOutputPackage()
      )
      .map { source =>
        val path = targetPath / os.RelPath(source.relPath)

        os.makeDir.all(path / os.up)
        os.write(path, source.source)

        PathRef(path)
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
    val scalaJsDom = mvn"org.scala-js::scalajs-dom::2.3.0"

    val flavourDeps = scalablyTypedFlavour() match
      case Flavour.Normal =>
        if (scalablyTypedUseScalaJsDomTypes()) Seq(scalaJsDom) else Seq.empty
      case Flavour.Slinky => Seq(mvn"me.shadaj::slinky-web::0.7.2")
      case Flavour.SlinkyNative =>
        Seq(
          mvn"me.shadaj::slinky-native::0.7.2".withDottyCompat(scalaVersion()),
          scalaJsDom
        )
      case Flavour.ScalajsReact =>
        Seq(mvn"com.github.japgolly.scalajs-react::core::2.1.1")

    super.mvnDeps() ++ Seq(
      mvn"com.olvind::scalablytyped-runtime::2.4.2"
    ) ++ flavourDeps
  }

  override def generatedSources: T[Seq[PathRef]] = {
    super.generatedSources() ++ scalablyTypedImportTask()
  }
}
