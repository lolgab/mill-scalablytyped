package com.github.lolgab.mill.scalablytyped.worker

import com.olvind.logging.Formatter
import com.olvind.logging.Logger
import org.scalablytyped.converter.internal.IArray
import org.scalablytyped.converter.internal.importer._
import org.scalablytyped.converter.internal.importer.build._
import org.scalablytyped.converter.internal.maps._
import org.scalablytyped.converter.internal.phases.GetDeps
import org.scalablytyped.converter.internal.phases.IsCircular
import org.scalablytyped.converter.internal.phases.Phase
import org.scalablytyped.converter.internal.phases.PhaseRes
import org.scalablytyped.converter.internal.scalajs._
import org.scalablytyped.converter.internal.scalajs.flavours.FlavourImpl

import java.time.ZonedDateTime
import scala.collection.immutable.SortedMap
import scala.collection.immutable.SortedSet
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

case class GenerateSourcesResult(
    libName: String,
    sources: IArray[(os.RelPath, String)],
    deps: Map[LibTsSource, GenerateSourcesResult]
)
object GenerateSourcesResult {
  object Unpack {
    def unapply(
        m: SortedMap[LibTsSource, GenerateSourcesResult]
    ): Some[SortedMap[LibTsSource, GenerateSourcesResult]] =
      Some(apply(m))

    def apply(
        m: SortedMap[LibTsSource, GenerateSourcesResult]
    ): SortedMap[LibTsSource, GenerateSourcesResult] = {
      val b = SortedMap.newBuilder[LibTsSource, GenerateSourcesResult]

      def go(tuple: (LibTsSource, GenerateSourcesResult)): Unit = {
        b += tuple
        tuple._2.deps.foreach(go)
      }

      m.foreach(go)

      b.result()
    }
  }
}

/** This phase goes from scala AST to compiled jar files on the local file
  * system
  */
class Phase3GenerateSources(
    versions: Versions,
    flavour: FlavourImpl
) extends Phase[LibTsSource, LibScalaJs, GenerateSourcesResult] {

  implicit val PathFormatter: Formatter[os.Path] = _.toString

  override def apply(
      source: LibTsSource,
      lib: LibScalaJs,
      getDeps: GetDeps[LibTsSource, GenerateSourcesResult],
      v4: IsCircular,
      _logger: Logger[Unit]
  ): PhaseRes[LibTsSource, GenerateSourcesResult] = {
    val logger = _logger.withContext("flavour", flavour.toString)

    getDeps(lib.dependencies.keys.map(x => x: LibTsSource).to[SortedSet])
      .map { case GenerateSourcesResult.Unpack(deps) =>
        val scope = new TreeScope.Root(
          libName = lib.scalaName,
          _dependencies = lib.dependencies.map { case (_, lib) =>
            lib.scalaName -> lib.packageTree
          },
          logger = logger,
          pedantic = false,
          outputPkg = flavour.outputPkg
        )

        GenerateSourcesResult(
          lib.libName,
          Printer(
            scope,
            new ParentsResolver,
            lib.packageTree,
            flavour.outputPkg,
            versions.scala
          ),
          deps
        )
      }
  }
}
