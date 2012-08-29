package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.exceptions.InvalidDocumentFactoryVerbosityException;

/**
 * Common class for test documents
 */
public abstract class AbstractTestDocument {
	
	/**
	 * The HTML test suite source file
	 */
	protected File sourceFile;
	
	/**
	 * The parsed document
	 */
	protected Document document;
	
	/**
	 * Create a new Test Document
	 * 
	 * @param sourceFile The XML source file
	 * @throws SAXException
	 * @throws IOException
	 */
	public AbstractTestDocument(File sourceFile) throws SAXException, IOException {
		this.setSourceFile(sourceFile);
		try {
			this.setDocument(new DocumentFactory(sourceFile).setVerbosityLevel(0).getDocument());
		}
		catch (InvalidDocumentFactoryVerbosityException e) {
			e.printStackTrace(); // will never happen
		}
	}
	
	/**
	 * Get the source file
	 * @return Returns the source file
	 */
	public File getSourceFile() {
		return sourceFile;
	}
	
	/**
	 * Set the source file
	 * @param sourceFile The source file
	 * @return Returns this object to maintain chainability
	 */
	public AbstractTestDocument setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
		return this;
	}
	
	/**
	 * Get the parsed document
	 * @return Returns the parsed document
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * Set the parsed document
	 * @param document The document
	 * @return Returns this object to maintain chainability
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
}
