package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;
import com.github.amercier.selenium.selenese.Action;
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
	 * @throws InvalidSeleneseCommandException 
	 * @throws DOMException 
	 * @throws UnknownSeleneseCommandException 
	 * @throws InvalidSeleneseCommandNameException 
	 * @throws SAXException
	 * @throws IOException
	 */
	public SeleneseTestCase getTestCase() throws DOMException, UnknownSeleneseCommandException, InvalidSeleneseCommandException {
		
		NodeList tableRows = document.getElementsByTagName("tr");

		// Create the test case object
		SeleneseTestCase test = new SeleneseTestCase(sourceFile.getName().replaceAll("\\.html$", ""));
		
		// Add the commands
		getLog().debug("Found " + (tableRows.getLength() - 1) + " commands");
		for (int i = 1; i < tableRows.getLength(); i++) {
			Element tableRow = (Element) tableRows.item(i);
			NodeList rowCells = tableRow.getElementsByTagName("td");
			
			String actionName = rowCells.item(0).getTextContent();
			
			// Arguments
			List<String> arguments = new LinkedList<String>();
			for (int j = 1; j < rowCells.getLength(); j++) {
				if(!rowCells.item(j).getTextContent().trim().equals("")) {
					arguments.add(rowCells.item(j).getTextContent());
				}
			}
			
			// Create the command
			Action action = null;
			try {
				action = Action.valueOf(actionName);
			}
			catch(IllegalArgumentException e) {
				throw new UnknownSeleneseCommandException(actionName);
			}
			SeleneseCommand command = new SeleneseCommand(action, arguments.toArray(new String[0]));
			getLog().debug("Found " + command);
			
			// Add the command to the test case
			test.addCommand(command);
		}
		
		return test;
	}

	
}
