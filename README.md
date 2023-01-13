# Scalablytyped Mill Plugin

Mill Plugin for [Scalablytyped](https://scalablytyped.org)

## Getting Started

The preferred way is to create a module in a separate build file.
You need to create a `scalablytyped.sc` file like:

```scala
import mill._, mill.scalalib._, mill.scalajslib._
import $ivy.`com.github.lolgab::mill-scalablytyped::0.1.2`
import com.github.lolgab.mill.scalablytyped._

object `scalablytyped-module` extends ScalaJSModule with ScalablyTyped {
  def scalaVersion = "3.1.3"
  def scalaJSVersion = "1.10.1"
}
```

Then you can import this module in your `build.sc` file:

```scala
import $file.scalablytyped
import mill._, mill.scalalib._, mill.scalajslib._

object app extends ScalaJSModule {
  def scalaVersion = "3.1.3"
  def scalaJSVersion = "1.10.1"
  def moduleDeps = Seq(scalablytyped.`scalablytyped-module`)
}
```

After that it will scan the directory for a `package.json` file and a `node_module` directory.
It will run ScalablyTyped to convert the libraries in `package.json` and then add them to `ivyDeps`.

### Mill version note

Make sure to use a Mill version greater than `0.10.1` otherwise the changes to the `build.sc` file will
re-trigger the Scalablytyped converter.
Also make sure that `import $file.scalablytyped` is one of the first imports in your `build.sc`, because
Ammonite recompiles all the next imported classes when a imported file changes. If the scalablytyped file
is imported earlier, there are less chances of doing useless recompilations with ScalablyTyped.

## Configuration

### scalablyTypedBasePath

The base path where package.json and node_modules are.
Defaults to the project root directory (the directory of `build.sc`).

### scalablyTypedIgnoredLibs

The typescript dependencies to ignore during the conversion

### scalablyTypedFlavour

The React flavour used by ScalablyTyped
Can be one of `Flavour.Normal`, `Flavour.Slinky`, `Flavour.SlinkyNative` and `Flavour.ScalajsReact` 

## Changelog

### 0.1.2

Support Mill `0.11.0-M1`

### 0.1.1

Support Mill `0.11.0-M0`

### 0.1.0

Update ScalablyTyped to `1.0.0-beta40`

### 0.0.7

Update ScalablyTyped to `1.0.0-beta39`

### 0.0.6

Update ScalablyTyped to `1.0.0-beta38`

### 0.0.5

Add support for `scalablyTypedFlavour`

### 0.0.4

Bump vulnerable log4j dependency

### 0.0.3

Improve error messages on failure

### 0.0.2

Add `scalablyTypedBasePath` and `scalablyTypedIgnoredLibs` configurations

### 0.0.1

First release
