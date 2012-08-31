package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.selenese.InvalidSeleneseCommandNameException;
import com.github.amercier.selenium.selenese.SeleneseTestSuite;
import com.github.amercier.selenium.selenese.log.DefaultLog;
import com.github.amercier.selenium.selenese.log.Log;

public class TestSuiteDocument extends AbstractTestDocument {
	
	/**
	 * Create a Test Suite document
	 * @param sourceFile The XML test suite file
	 * @paran log        The log
	 * @throws SAXException
	 * @throws IOException
	 */
	public TestSuiteDocument(File sourceFile, Log log) throws SAXException, IOException {
		super(sourceFile, log);
	}
	
	/**
	 * Create a Test Suite document
	 * @param sourceFile The XML test suite file
	 * @throws SAXException
	 * @throws IOException
	 */
	public TestSuiteDocument(File sourceFile) throws SAXException, IOException {
		this(sourceFile, new DefaultLog());
	}
	
	/**
	 * Get the test suite from the document
	 * 
	 * @return Returns the test suite
	 * @throws SAXException
	 * @throws IOException
	 * @throws InvalidSeleneseCommandException 
	 * @throws DOMException 
	 * @throws InvalidSeleneseCommandNameException 
	 */
	public SeleneseTestSuite getTestSuite() throws SAXException, IOException, DOMException, InvalidSeleneseCommandNameException {
		
		// Create the test suite object
		SeleneseTestSuite suite = new SeleneseTestSuite(sourceFile.getName().replaceAll("\\.html$", ""));
		
		// Add the test cases
		Element table = (Element) document.getElementsByTagName("table").item(0);
		NodeList tableRows = table.getElementsByTagName("tr");
		
		getLog().debug("Found " + (tableRows.getLength() - 1) + " test cases");
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
								),
							this.getLog()
						)
						.getTestCase()
						.setName(link.getTextContent()) // update the name with the one found in the suite
				);
			
			getLog().debug("Added " + link.getTextContent() + " successfully");
		}
		
		return suite;
	}
}
