package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.selenese.SeleneseCommand;
import com.github.amercier.selenium.selenese.SeleneseTestCase;
import com.github.amercier.selenium.selenese.log.DefaultLog;
import com.github.amercier.selenium.selenese.log.Log;

public class TestCaseDocument extends AbstractTestDocument {
	
	/**
	 * Create a Test Case document
	 * 
	 * @param sourceFile The XML test suite file
	 * @param log        The log
	 * @throws SAXException
	 * @throws IOException
	 */
	public TestCaseDocument(File sourceFile, Log log) throws SAXException, IOException {
		super(sourceFile, log);
	}
	
	/**
	 * Create a Test Case document, with a default Selenese Log
	 * 
	 * @param sourceFile The XML test suite file
	 * @throws SAXException
	 * @throws IOException
	 */
	public TestCaseDocument(File sourceFile) throws SAXException, IOException {
		this(sourceFile, new DefaultLog());
	}

	/**
	 * Get the test case from the document
	 * 
	 * @return Returns the test case
	 * @throws SAXException
	 * @throws IOException
	 */
	public SeleneseTestCase getTestCase() {
		
		NodeList tableRows = document.getElementsByTagName("tr");

		// Create the test case object
		SeleneseTestCase test = new SeleneseTestCase(sourceFile.getName().replaceAll("\\.html$", ""));
		
		// Add the commands
		for (int i = 1; i < tableRows.getLength(); i++) {
			Element tableRow = (Element) tableRows.item(i);
			NodeList rowCells = tableRow.getElementsByTagName("td");
			
			// Create the command
			SeleneseCommand command = new SeleneseCommand(rowCells.item(0).getTextContent());
			
			// Add the arguments
			for (int j = 1; j < rowCells.getLength(); j++) {
				command.addArgument(rowCells.item(j).getTextContent());
			}
		}
		
		return test;
	}

	
}
