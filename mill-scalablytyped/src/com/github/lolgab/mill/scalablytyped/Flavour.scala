package com.github.lolgab.mill.scalablytyped

import upickle.default._

sealed trait Flavour
object Flavour {
  case object Normal extends Flavour
  case object Slinky extends Flavour
  case object SlinkyNative extends Flavour
  case object ScalajsReact extends Flavour

  implicit val normalRW: ReadWriter[Flavour.Normal.type] =
    macroRW[Flavour.Normal.type]
  implicit val slinkyRW: ReadWriter[Flavour.Slinky.type] =
    macroRW[Flavour.Slinky.type]
  implicit val slinkyNativeRW: ReadWriter[Flavour.SlinkyNative.type] =
    macroRW[Flavour.SlinkyNative.type]
  implicit val scalajsReactRW: ReadWriter[Flavour.ScalajsReact.type] =
    macroRW[Flavour.ScalajsReact.type]
  implicit val rw: ReadWriter[Flavour] = macroRW[Flavour]
}
