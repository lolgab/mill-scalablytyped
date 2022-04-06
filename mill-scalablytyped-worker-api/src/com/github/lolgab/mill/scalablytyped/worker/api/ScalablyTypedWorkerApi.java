package com.github.lolgab.mill.scalablytyped.worker.api;

import java.nio.file.Path;

public interface ScalablyTypedWorkerApi {
	ScalablyTypedWorkerDep[] scalablytypedImport(
		Path basePath, 
		Path ivyHomePath, 
		Path targetPath, 
		String scalaVersion, 
		String scalaJSVersion, 
		String[] ignoredLibs,
		StMillFlavour flavour
		);
}
