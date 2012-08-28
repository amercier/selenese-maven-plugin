package com.github.amercier.selenium.maven.document;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.maven.exceptions.InvalidDocumentFactoryVerbosityException;

public class TestSuiteDocument {
	
	File sourceFile;
	Document testSuiteDocument;
	
	public TestSuiteDocument(File testSuiteFile) throws SAXException, IOException {
		this.sourceFile = testSuiteFile;
		try {
			this.testSuiteDocument = new DocumentFactory(testSuiteFile).setVerbosityLevel(0).getDocument();
		}
		catch (InvalidDocumentFactoryVerbosityException e) {
			e.printStackTrace(); // will never happen
		}
	}

}
