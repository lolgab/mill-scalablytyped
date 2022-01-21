# Scalablytyped Mill Plugin

Mill Plugin for [Scalablytyped](https://scalablytyped.org)

## Getting Started

After importing it in the `build.sc` file:

```scala
import $ivy.`com.github.lolgab::mill-scalablytyped::x.y.z`
import com.github.lolgab.mill.scalablytyped._
```

this plugin can be mixed in a `ScalaJSModule`:

```scala
object module extends ScalaJSModule with ScalablyTyped {
  // ... other settings
}
```

After that it will scan the directory for a `package.json` file and a `node_module` directory.
It will run ScalablyTyped to convert the libraries in `package.json` and then add them to `ivyDeps`.

## Configuration

There are no configurations available for now

## Changelog

### 0.0.1

First release
