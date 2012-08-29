package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

	public SeleneseTestCase[] getTestCases() {
		List<SeleneseTestCase> testCases = new LinkedList<SeleneseTestCase>();
		
		
		
		return testCases.toArray(new SeleneseTestCase[0]);
	}

	
}
