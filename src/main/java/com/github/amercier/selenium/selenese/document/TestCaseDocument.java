package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import com.github.amercier.selenium.selenese.SeleneseTestCase;

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

	/**
	 * Get the test case from the document
	 * 
	 * @return Returns the test case
	 * @throws SAXException
	 * @throws IOException
	 */
	public SeleneseTestCase getTestCase() {
		
		// Create the test case object
		SeleneseTestCase test = new SeleneseTestCase(sourceFile.getName().replaceAll("/\\.html$", ""));
		
		// Add the commands
		
		
		return test;
	}

	
}
