package com.github.lolgab.mill.scalablytyped

import upickle.default._

private[scalablytyped] case class PackageJson(
    dependencies: Map[String, String] = Map.empty,
    devDependencies: Map[String, String] = Map.empty,
    peerDependencies: Map[String, String] = Map.empty
)
private[scalablytyped] object PackageJson {
  implicit val rw: ReadWriter[PackageJson] = macroRW
}
