package com.github.lolgab.mill.scalablytyped.worker

import java.nio.file.Path

import com.github.lolgab.mill.scalablytyped.worker.api._
import com.olvind.logging.stdout
import com.olvind.logging.storing
import com.olvind.logging.LogLevel
import com.olvind.logging.Logger
import fansi.Attr
import fansi.Color
import fansi.Str
import org.scalablytyped.converter.internal.IArray
import org.scalablytyped.converter.internal.importer._
import org.scalablytyped.converter.internal.importer.build.PublishedSbtProject
import org.scalablytyped.converter.internal.importer.build.SbtProject
import org.scalablytyped.converter.internal.importer.documentation.Npmjs
import org.scalablytyped.converter.internal.phases.PhaseListener.NoListener
import org.scalablytyped.converter.internal.phases.PhaseRes
import org.scalablytyped.converter.internal.phases.PhaseRunner
import org.scalablytyped.converter.internal.phases.RecPhase
import org.scalablytyped.converter.internal.scalajs.Name
import org.scalablytyped.converter.internal.scalajs.Dep
import org.scalablytyped.converter.internal.scalajs.Versions
import org.scalablytyped.converter.internal.sets.SetOps
import org.scalablytyped.converter.internal.ts.CalculateLibraryVersion.PackageJsonOnly
import org.scalablytyped.converter.internal.ts.PackageJson
import org.scalablytyped.converter.internal.ts.TsIdentLibrary
import org.scalablytyped.converter.internal.constants
import org.scalablytyped.converter.internal.files
import org.scalablytyped.converter.internal.sets
import org.scalablytyped.converter.internal.BuildInfo
import org.scalablytyped.converter.internal.InFolder
import org.scalablytyped.converter.internal.Json
import org.scalablytyped.converter.Flavour
import org.scalablytyped.converter.Selection

import scala.collection.immutable.SortedSet
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class ScalablyTypedWorkerImpl extends ScalablyTypedWorkerApi {
  class Paths(base: os.Path) {
    val node_modules: Option[os.Path] =
      Option(base / "node_modules").filter(files.exists)
    val packageJson: Option[os.Path] =
      Option(base / "package.json").filter(files.exists)
  }

  def toScalablyTyped(flavour: ScalablyTypedWorkerFlavour) = flavour match {
    case ScalablyTypedWorkerFlavour.Normal => Flavour.Normal
    case ScalablyTypedWorkerFlavour.ScalajsReact => Flavour.ScalajsReact
    case ScalablyTypedWorkerFlavour.Slinky => Flavour.Slinky
    case ScalablyTypedWorkerFlavour.SlinkyNative => Flavour.SlinkyNative
  }

  override def scalablytypedImport(
      basePath: java.nio.file.Path,
      ivyHomePath: java.nio.file.Path,
      scalaVersion: String,
      wanted: Array[String],
      ignoredLibs: Array[String],
      useScalaJsDomTypes: Boolean,
      includeDev: Boolean,
      flavour: ScalablyTypedWorkerFlavour,
      outputPackage: String
  ): Array[ScalablyTypedWorkerSource] = {

    val DefaultOptions = ConversionOptions(
      useScalaJsDomTypes = useScalaJsDomTypes,
      outputPackage = Name(outputPackage),
      enableScalaJsDefined = Selection.All,
      flavour = toScalablyTyped(flavour),
      ignored = SortedSet("typescript") ++ ignoredLibs,
      stdLibs = SortedSet("es6"),
      expandTypeMappings = EnabledTypeMappingExpansion.DefaultSelection,
      versions = Versions(
        Versions.Scala(scalaVersion),
        Versions.ScalaJs("unused")
      ),
      organization = "org.scalablytyped",
      enableReactTreeShaking = Selection.None,
      enableLongApplyMethod = false,
      privateWithin = None,
      useDeprecatedModuleNames = false
    )

    val inDir = os.Path(basePath)
    val paths = new Paths(inDir)
    val conversion = DefaultOptions
    val includeProject = false

    val parseCachePath = Some(
      files.existing(constants.defaultCacheFolder / "parse").toNIO
    )
    val t0 = System.currentTimeMillis

    val logger: Logger[(Array[Logger.Stored], Unit)] =
      storing().zipWith(stdout.filter(LogLevel.warn))

    def table(Key: Attr)(kvs: (String, String)*): Str = {
      val headerLength =
        kvs.map { case (header, _) => header }.maxBy(_.length).length + 1
      val massaged = kvs.flatMap { case (header, value) =>
        Seq[Str](Key(header.padTo(headerLength, ' ')), value, "\n")
      }
      Str.join(massaged)
    }

    val packageJsonPath = paths.packageJson.getOrElse(
      sys.error(s"$inDir does not contain package.json")
    )
    val nodeModulesPath = paths.node_modules.getOrElse(
      sys.error(s"$inDir does not contain node_modules")
    )
    require(
      files.exists(nodeModulesPath / "typescript" / "lib"),
      "must install typescript"
    )

    val packageJson = Json.force[PackageJson](packageJsonPath)

    val projectSource: Option[LibTsSource.FromFolder] =
      if (includeProject)
        Some(
          LibTsSource.FromFolder(InFolder(inDir), TsIdentLibrary(inDir.last))
        )
      else None

    require(wanted.nonEmpty, s"All libraries in package.json ignored")
    val wantedLibs: SortedSet[TsIdentLibrary] =
      SortedSet(wanted.map(TsIdentLibrary(_)): _*)

    val bootstrapped = Bootstrap.fromNodeModules(
      InFolder(nodeModulesPath),
      conversion,
      wantedLibs
    )

    val sources: Vector[LibTsSource] = {
      bootstrapped.initialLibs match {
        case Left(unresolved) => sys.error(unresolved.msg)
        case Right(initial) => projectSource.foldLeft(initial)(_ :+ _)
      }
    }

    val publishLocalFolder = os.Path(ivyHomePath)

    println(
      table(fansi.Color.LightBlue)(
        "directory" -> inDir.toString,
        "includeDev" -> includeDev.toString,
        "includeProject" -> includeProject.toString,
        "wantedLibs" -> sources.map(s => s.libName.value).mkString(", "),
        "useScalaJsDomTypes" -> conversion.useScalaJsDomTypes.toString,
        "flavour" -> conversion.flavour.toString,
        "outputPackage" -> conversion.outputPackage.unescaped,
        "enableScalaJsDefined" -> conversion.enableScalaJsDefined
          .map(_.value)
          .toString,
        "stdLibs" -> conversion.stdLibs.toString,
        "expandTypeMappings" -> conversion.expandTypeMappings
          .map(_.value)
          .toString,
        "ignoredLibs" -> conversion.ignoredLibs.map(_.value).toString,
//            "ignoredModulePrefixes" -> conversion.ignoredModulePrefixes.toString,
        "versions" -> conversion.versions.toString,
        "organization" -> conversion.organization,
        "enableLongApplyMethod" -> conversion.enableLongApplyMethod.toString
      )
    )

    val Pipeline =
      RecPhase[LibTsSource]
        .next(
          new Phase1ReadTypescript(
            calculateLibraryVersion = PackageJsonOnly,
            resolve = bootstrapped.libraryResolver,
            ignored = conversion.ignoredLibs,
            ignoredModulePrefixes = conversion.ignoredModulePrefixes,
            pedantic = false,
            parser = PersistingParser(
              parseCachePath,
              bootstrapped.inputFolders,
              logger.void
            ),
            expandTypeMappings = conversion.expandTypeMappings
          ),
          "typescript"
        )
        .next(
          new Phase2ToScalaJs(
            pedantic = false,
            scalaVersion = conversion.versions.scala,
            enableScalaJsDefined = conversion.enableScalaJsDefined,
            outputPkg = conversion.outputPackage,
            flavour = conversion.flavourImpl,
            useDeprecatedModuleNames = false
          ),
          "scala.js"
        )
        .next(
          new PhaseFlavour(
            conversion.flavourImpl,
            maybePrivateWithin = conversion.privateWithin
          ),
          conversion.flavourImpl.toString
        )
        .next(
          new Phase3GenerateSources(
            versions = conversion.versions,
            flavour = conversion.flavourImpl
          ),
          "build"
        )

    val results =
      sources.map { source =>
        source -> PhaseRunner
          .go(
            Pipeline,
            source,
            Nil,
            (_: LibTsSource) => logger.void,
            NoListener
          )
      }.toMap

    val td = System.currentTimeMillis - t0
    logger.warn(td)

    val failures: Map[LibTsSource, Either[Throwable, String]] =
      results
        .collect { case (_, PhaseRes.Failure(errors)) => errors }
        .reduceOption(_ ++ _)
        .getOrElse(Map.empty)

    if (failures.nonEmpty) {
      val failuresLibs =
        failures.keys.map(_.libName.value).filter(_ != "std").toSeq
      if (failuresLibs.nonEmpty) {
        val allIgnoredLibs = ignoredLibs ++ failuresLibs
        val allIgnoredLibsString = allIgnoredLibs
          .map(lib => s""""$lib"""")
          .mkString(", ")
        println(
          Color.Red(
            s"""Failure: You might try to set:
            |  def scalablyTypedIgnoredLibs = Seq($allIgnoredLibsString)""".stripMargin
          )
        )
      } else if (scalaVersion == "3.1.2") {
        Color.Red("""Failure: There is a known problem with Scala 3.1.2
          |Try to downgrade to Scala 3.1.1""".stripMargin)
      } else {
        Color.Red("Failure. Unknown problem.")
      }

      failures.foreach {
        case (source, Left(value)) =>
          println(s"${source.libName.value}: (${source.path})")
          value.printStackTrace()
        case (source, Right(value)) =>
          println(s"${source.libName.value}: $value (${source.path})")
      }
      throw new Exception
    } else {
      val allSuccesses = {
        def go(
            source: LibTsSource,
            result: GenerateSourcesResult
        ): Map[LibTsSource, GenerateSourcesResult] =
          Map(source -> result) ++
            result.deps.flatMap { case (k, v) => go(k, v) }

        results
          .flatMap {
            case (s, PhaseRes.Ok(res)) => go(s, res)
            case _ => Map.empty[LibTsSource, GenerateSourcesResult]
          }
      }

      println()
      println(
        s"Successfully converted ${allSuccesses.keys.map(x => Color.Green(x.libName.value)).mkString(", ")}"
      )
      allSuccesses.values.flatMap { sources =>
        sources.sources.map { case (relPath, source) =>
          new ScalablyTypedWorkerSource(relPath.toString(), source)
        }.toVector
      }.toArray

    }
  }

  override def defaultOutputPackage(): String = Name.typings.unescaped
}
