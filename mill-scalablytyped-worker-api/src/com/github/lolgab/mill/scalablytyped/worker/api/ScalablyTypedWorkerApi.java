package com.github.lolgab.mill.scalablytyped.worker.api;

import java.nio.file.Path;

public interface ScalablyTypedWorkerApi {
	ScalablyTypedWorkerSource[] scalablytypedImport(Path basePath, Path ivyHomePath, String scalaVersion,
			String[] ignoredLibs, boolean useScalaJsDomTypes, boolean includeDev,
			ScalablyTypedWorkerFlavour flavour, String outputPackage);

	String defaultOutputPackage();
}
