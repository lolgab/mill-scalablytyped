package com.github.lolgab.mill.scalablytyped

sealed trait Flavour
object Flavour {
  case object Normal extends Flavour
  case object Slinky extends Flavour
  case object SlinkyNative extends Flavour
  case object ScalajsReact extends Flavour
}
