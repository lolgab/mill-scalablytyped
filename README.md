# Scalablytyped Mill Plugin

Mill Plugin for [Scalablytyped](https://scalablytyped.org)

## Getting Started

The preferred way is to create a separate module for the scalablytyped generated
code, and then add it to your application's `moduleDeps`:

```scala
//| mill-version: 1.0.3
package build

import mill.*, mill.scalalib.*, mill.scalajslib.*
import $ivy.`com.github.lolgab::mill-scalablytyped::0.2.0`
import com.github.lolgab.mill.scalablytyped.*

trait Base extends ScalaJSModule {
  def scalaVersion = "3.7.2"
  def scalaJSVersion = "1.19.0"
}

object `scalablytyped-module` extends Base with ScalablyTyped

object app extends Base {
  def moduleDeps = Seq(`scalablytyped-module`)
}
```

After that it will scan the directory for a `package.json` file and a `node_module` directory.
It will run ScalablyTyped to convert the libraries in `package.json` and then add them to `ivyDeps`.

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

### 0.2.0

Update Mill to version 1

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
