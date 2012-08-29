package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.exceptions.InvalidDocumentFactoryVerbosityException;

/**
 * A common File-to-Document factory. Uses 
 *
 */
public class DocumentFactory {
	
	/**
	 * Verbosity constants
	 */
	public static final int VERBOSITY_DEFAULT = 1;
	public static final int VERBOSITY_MIN = 0;
	public static final int VERBOSITY_MAX = 2;
	
	
	/**
	 * The source file
	 */
	protected File file;
	
	/**
	 * Verbosity level :
	 *   - 0: displays nothing
	 *   - 1: (default) display warnings
	 *   - 2: Display warnings + debug messages
	 */
	protected int verbosityLevel;

	/**
	 * Create a new Document factory
	 * @param file The source file
	 */
	public DocumentFactory(File file) {
		this.file = file;
		try {
			this.setVerbosityLevel(VERBOSITY_DEFAULT);
		}
		catch (InvalidDocumentFactoryVerbosityException e) {
			e.printStackTrace(); // will never happen
		}
	}
	
	/**
	 * Get the {@link #verbosityLevel verbosity level}
	 */
	public int getVerbosityLevel() {
		return verbosityLevel;
	}
	
	/**
	 * Set the {@link #verbosityLevel verbosity level}
	 * @param verbosityLevel The new verbosity level, between {@link #VERBOSITY_MIN} and {@link #VERBOSITY_MAX}
	 * @return Returns this object to maintain chainability
	 * @throws InvalidDocumentFactoryVerbosityException If the verbosity level is invalid
	 */
	public DocumentFactory setVerbosityLevel(int verbosityLevel) throws InvalidDocumentFactoryVerbosityException {
		if(verbosityLevel < VERBOSITY_MIN || verbosityLevel > VERBOSITY_MAX) {
			throw new InvalidDocumentFactoryVerbosityException(verbosityLevel);
		}
		this.verbosityLevel = verbosityLevel;
		return this;
	}
	
	/**
	 * Parse the {@link #file source file} and convert it into an XML Document.
	 * 
	 * @return The parsed Docyment
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getDocument() throws SAXException, IOException {
		Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		
			// Prevents resolving namespaces (useless)
			builder.setEntityResolver(new EntityResolver() {
				public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					DocumentFactory.this.displayWarningMessage("Ignoring entity " + publicId + ", " + systemId);
					return new org.xml.sax.InputSource(new java.io.StringReader(""));
				}
			});
			displayDebugMessage("Parsing " + file.getName() + "...");
			document = builder.parse(file.toString());
			displayDebugMessage("Parsed " + file.getName() + " successfully");
		
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace(); // Should not happen as we use the default parser configuration
		}
		
		return document;
	}
	
	/**
	 * Display a warning message
	 * @param message The message to display
	 */
	public void displayWarningMessage(String message) {
		if(this.getVerbosityLevel() >= 1) {
			System.err.println(message);
		}
	}
	
	/**
	 * Display a debug message
	 * @param message The message to display
	 */
	public void displayDebugMessage(String message) {
		if(this.getVerbosityLevel() >= 2) {
			System.err.println(message);
		}
	}
}
