package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.selenese.SeleneseTestSuite;

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
	 * Get the test suite from the document
	 * 
	 * @return Returns the test suite
	 * @throws SAXException
	 * @throws IOException
	 */
	public SeleneseTestSuite getTestSuite() throws SAXException, IOException {
		
		// Create the test suite object
		SeleneseTestSuite suite = new SeleneseTestSuite(sourceFile.getName().replaceAll("/\\.html$", ""));
		
		// Add the test cases
		Element table = (Element) document.getElementsByTagName("table").item(0);
		NodeList tableRows = table.getElementsByTagName("tr");
		for (int i = 1; i < tableRows.getLength(); i++) {
			Element tableRow = (Element) tableRows.item(i);
			Element cell = (Element) tableRow.getElementsByTagName("td").item(0);
			Element link = (Element) cell.getElementsByTagName("a").item(0);
			
			// Add the test case
			suite.addTestCase(
					new TestCaseDocument(
							new File(
									sourceFile.getParent(),
									link.getAttribute("href")
								)
						)
						.getTestCase()
						.setName(link.getTextContent()) // update the name with the one found in the suite
				);
		}
		
		return suite;
	}
}
