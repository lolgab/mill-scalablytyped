# Scalablytyped Mill Plugin

Mill Plugin for [Scalablytyped](https://scalablytyped.org)

## Getting Started

The preferred way is to create a separate module for the scalablytyped generated
code, and then add it to your application's `moduleDeps`:

```scala
// build.mill
package build

import mill._, mill.scalalib._, mill.scalajslib._
import $ivy.`com.github.lolgab::mill-scalablytyped::0.1.15`
import com.github.lolgab.mill.scalablytyped._

trait Base extends ScalaJSModule {
  def scalaVersion = "3.3.4"
  def scalaJSVersion = "1.17.0"
}

object `scalablytyped-module` extends Base with ScalablyTyped

object app extends Base {
  def moduleDeps = Seq(`scalablytyped-module`)
}
```

After that it will scan the directory for a `package.json` file and a `node_module` directory.
It will run ScalablyTyped to convert the libraries in `package.json` and then add them to `ivyDeps`.

### Mill version note (Mill 0.10 or older)

If you are using Mill 0.10, make sure to use a Mill version greater than `0.10.1` otherwise the changes
to the `build.sc` file will re-trigger the Scalablytyped converter.
Also make sure that `import $file.scalablytyped` is one of the first imports in your `build.sc`, because
Ammonite recompiles all the next imported classes when a imported file changes. If the scalablytyped file
is imported earlier, there are less chances of doing useless recompilations with ScalablyTyped.

To avoid rerunning the scalablytyped compiler at every build file change,
the preferred way is to create a module in a separate build file.
You need to create a `scalablytyped.sc` file like:

```scala
import mill._, mill.scalalib._, mill.scalajslib._
import $ivy.`com.github.lolgab::mill-scalablytyped::0.1.12`
import com.github.lolgab.mill.scalablytyped._

object `scalablytyped-module` extends ScalaJSModule with ScalablyTyped {
  def scalaVersion = "3.2.2"
  def scalaJSVersion = "1.13.0"
}
```

Then you can import this module in your `build.sc` file:

```scala
import $file.scalablytyped
import mill._, mill.scalalib._, mill.scalajslib._

object app extends ScalaJSModule {
  def scalaVersion = "3.2.2"
  def scalaJSVersion = "1.13.0"
  def moduleDeps = Seq(scalablytyped.`scalablytyped-module`)
}
```

## Configuration

### scalablyTypedBasePath

The base path where package.json and node_modules are.
Defaults to the project root directory (the directory of `build.sc`).

### scalablyTypedIgnoredLibs

The typescript dependencies to ignore during the conversion

### useScalaJsDomTypes

When true (which is the default) uses scala-js-dom types when possible instead of types we translate from typescript in std

### scalablyTypedFlavour

The React flavour used by ScalablyTyped
Can be one of `Flavour.Normal`, `Flavour.Slinky`, `Flavour.SlinkyNative` and `Flavour.ScalajsReact` 

### scalablyTypedIncludeDev

If `true` generate facades for dev dependencies as well. Default: `false`

### scalablyTypedOutputPackage

Adjusts the top-level package name of the generated code.

## Changelog

### 0.1.15

Add support for `scalablyTypedIncludeDev`

### 0.1.14

Add support for `useScalaJsDomTypes`

### 0.1.13

Update ScalablyTyped to `1.0.0-beta44`

### 0.1.12

Update ScalablyTyped to `1.0.0-beta43`

### 0.1.11

Update Mill `0.11` to `0.11.0`

### 0.1.10

Update Mill `0.11` to `0.11.0-M10`

### 0.1.9

Update Mill `0.11` to `0.11.0-M10`

### 0.1.8

Update Mill `0.11` to `0.11.0-M9`

### 0.1.7

Update Mill `0.11` to `0.11.0-M8`

### 0.1.6

Update Mill `0.11` to `0.11.0-M7`

### 0.1.5

Update Mill `0.11` to `0.11.0-M6`

### 0.1.4

Update ScalablyTyped to `1.0.0-beta41` and Mill `0.11` to `0.11.0-M2`

### 0.1.3

Update Mill `0.11.0-M1` to `0.11.0-M1-29-8f872d`

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
