package com.github.amercier.selenium.maven.document;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

public class TestCaseDocument extends AbstractTestDocument {
	
	/**
	 * Create a Test Case document
	 * @param sourceFile The XML test suite file
	 * @throws SAXException
	 * @throws IOException
	 */
	public TestCaseDocument(File sourceFile) throws SAXException, IOException {
		super(sourceFile);
	}

	
}
