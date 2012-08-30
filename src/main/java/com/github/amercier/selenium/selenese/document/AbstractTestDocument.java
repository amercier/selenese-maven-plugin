package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.selenese.log.DefaultLog;
import com.github.amercier.selenium.selenese.log.DefaultLoggable;
import com.github.amercier.selenium.selenese.log.Log;

/**
 * Common class for test documents
 */
public abstract class AbstractTestDocument extends DefaultLoggable {
	
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
	 * @param log        The log
	 * @throws SAXException
	 * @throws IOException
	 */
	public AbstractTestDocument(File sourceFile, Log log) throws SAXException, IOException {
		super(log);
		this.setSourceFile(sourceFile);
		this.setDocument(new DocumentFactory(sourceFile, log).getDocument());
	}
	
	/**
	 * Create a new Test Document
	 * 
	 * @param sourceFile The XML source file
	 * @param log        The log
	 * @throws SAXException
	 * @throws IOException
	 */
	public AbstractTestDocument(File sourceFile) throws SAXException, IOException {
		this(sourceFile, new DefaultLog());
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
