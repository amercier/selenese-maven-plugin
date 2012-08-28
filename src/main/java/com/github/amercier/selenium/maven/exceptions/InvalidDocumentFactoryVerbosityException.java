package com.github.amercier.selenium.maven.exceptions;

import com.github.amercier.selenium.maven.document.DocumentFactory;

@SuppressWarnings("serial")
public class InvalidDocumentFactoryVerbosityException extends Exception {

	public InvalidDocumentFactoryVerbosityException(int verbosity) {
		super("Invalid verbosity " + verbosity + ", must be between " + DocumentFactory.VERBOSITY_MIN + " and " + DocumentFactory.VERBOSITY_MAX + ".");
	}
}
