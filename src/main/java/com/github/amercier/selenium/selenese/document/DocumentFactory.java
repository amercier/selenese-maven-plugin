package com.github.amercier.selenium.selenese.document;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.selenese.log.DefaultLog;
import com.github.amercier.selenium.selenese.log.DefaultLoggable;
import com.github.amercier.selenium.selenese.log.Log;

/**
 * A common File-to-Document factory. Uses 
 *
 */
public class DocumentFactory extends DefaultLoggable {
	
	/**
	 * The source file
	 */
	protected File file;
	
	/**
	 * Create a new Document factory
	 * @param file The source file
	 */
	public DocumentFactory(File file) {
		this(file, new DefaultLog());
	}
	
	/**
	 * Create a new Document factory
	 * @param file The source file
	 * @param log  The Selenese log
	 */
	public DocumentFactory(File file, Log log) {
		super(log);
		this.setFile(file);
	}
	
	/**
	 * Set the file
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Get the file
	 * @return Returns the source file
	 */
	public File getFile() {
		return file;
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
					DocumentFactory.this.getLog().debug("Ignoring entity " + publicId + ", " + systemId);
					return new org.xml.sax.InputSource(new java.io.StringReader(""));
				}
			});
			getLog().debug("Parsing " + file.getName() + "...");
			document = builder.parse(file.toString());
			getLog().debug("Parsed " + file.getName() + " successfully");
		
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace(); // Should not happen as we use the default parser configuration
		}
		
		return document;
	}
	
}
