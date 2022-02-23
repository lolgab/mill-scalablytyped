# Scalablytyped Mill Plugin

Mill Plugin for [Scalablytyped](https://scalablytyped.org)

## Getting Started

The preferred way is to create a module in a separate build file.
You need to create a `scalablytyped.sc` file like:

```scala
import mill._, mill.scalalib._, mill.scalajslib._
import $ivy.`com.github.lolgab::mill-scalablytyped::x.y.z`
import com.github.lolgab.mill.scalablytyped._

object module extends ScalaJSModule with ScalablyTyped {
  def scalaVersion = "3.1.0"
  def scalaJSVersion = "1.8.0"
}
```

Then you can import this module in your `build.sc` file:

```scala
import mill._, mill.scalalib._, mill.scalajslib._
import $file.scalablytyped

object app extends ScalaJSModule {
  def scalaVersion = "3.1.0"
  def scalaJSVersion = "1.8.0"
  def moduleDeps = Seq(scalablytyped.module)
}
```

After that it will scan the directory for a `package.json` file and a `node_module` directory.
It will run ScalablyTyped to convert the libraries in `package.json` and then add them to `ivyDeps`.

### Mill version note

Make sure to use a Mill version greater than `0.10.0-60-4dcea9` otherwise the changes to the `build.sc`
file will re-trigger the Scalablytyped converter.

## Configuration

### scalablyTypedBasePath

The base path where package.json and node_modules are.
Defaults to the project root directory (the directory of `build.sc`).

### scalablyTypedIgnoredLibs

The typescript dependencies to ignore during the conversion

## Changelog

### 0.0.1

First release
