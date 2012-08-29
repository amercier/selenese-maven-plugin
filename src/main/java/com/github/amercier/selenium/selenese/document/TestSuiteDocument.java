package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

public class TestSuiteDocument extends AbstractTestDocument {
	
	/**
	 * Create a Test Suite document
	 * @param sourceFile The XML test suite file
	 * @throws SAXException
	 * @throws IOException
	 */
	public TestSuiteDocument(File sourceFile) throws SAXException, IOException {
		super(sourceFile);
	}

	/**
	 * Get the test case documents of this test suite
	 * @return Returns a lsit of {@link TestCaseDocuments Test case documents}
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public TestCaseDocument[] getTestCaseDocuments() throws SAXException, IOException {
		List<TestCaseDocument> documents = new LinkedList<TestCaseDocument>();
		
		
		
		return documents.toArray(new TestCaseDocument[0]);
	}
}
