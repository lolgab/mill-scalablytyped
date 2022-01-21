package com.github.lolgab.mill.scalablytyped.worker.api;

import java.nio.file.Path;

public interface ScalablyTypedWorkerApi {
  ScalablyTypedWorkerDep[] scalablytypedImport(Path pw, Path ivyHomePath, String scalaVersion, String scalaJSVersion);
}
