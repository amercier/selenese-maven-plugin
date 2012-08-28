package com.github.amercier.selenese_maven_plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.thoughtworks.selenium.CommandProcessor;

/**
 * Goal which sends Selenese HTML tests to be run by a remote or local Selenium
 * 2 Server/Grid Hub
 * 
 * @goal  integration-test
 * @phase integration-test
 */
public class SeleneseMojo extends AbstractMojo {
	
	/**
	 * The test base URL
	 * 
	 * @parameter expression="${selenium.baseUrl}"
	 * @required
	 */
	public String baseUrl;
	
	/**
	 * The test suite to run
	 * 
	 * @parameter expression="${selenium.testSuite}"
	 */
	public File testSuite;
	
	/**
	 * The test case to run
	 * 
	 * @parameter expression="${selenium.testCase}"
	 */
	public File testCase;
	
	/**
	 * Server or Grid Hub address
	 * 
	 * @parameter expression="${selenium.server.host}"
	 */
	public String host = "localhost";
	
	/**
	 * Server or Grid Hub port number
	 * 
	 * @parameter expression="${selenium.server.port}"
	 */
	public int port = 4444;
	
	/**
	 * Comma-separated list of browsers, to be launched by the Selenium
	 * Server/Grid
	 * 
	 * @parameter expression="${selenium.browser}"
	 */
	public String[] browsers = new String[] { "*firefox" };
	
	/**
	 * The file to write the test results reports
	 * 
	 * @parameter expression="${selenium.resultsFile}"
	 * @required
	 */
	public File resultsFile;
	
	/**
	 * JavaScript user extensions
	 * @parameter expression="${selenium.jsExtensions}"
	 */
	public File[] jsExtensions;
	
	/**
	 * Run the tests
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		try {
			
			// Check testCase XOR testSuite
			if (testCase == null && testSuite == null) {
				throw new RuntimeException("No testCase or testSuite file specified");
			}
			else if (testCase != null && testSuite != null) {
				throw new RuntimeException("A testCase and testSuite file cannot both be specified");
			}
			
			// Instanciate the results writer
			setResultsWriter(new FileWriter(resultsFile));
			
			// Run either the test case (if specified) or the test suite
			if(testCase != null) {
				for(String browser : browsers) {
					runTestCase(testCase, browser);
				}
			}
			else {
				for(String browser : browsers) {
					runTestSuite(testSuite, browser);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if (getResultsWriter() != null) {
				try {
					getResultsWriter().close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	protected OutputStreamWriter   _resultsWriter;
	protected HtmlCommandProcessor _commandProcessor;
	
	protected OutputStreamWriter getResultsWriter() {
		return _resultsWriter;
	}
	
	protected void setResultsWriter(OutputStreamWriter writer) {
		_resultsWriter = writer;
	}
	
	protected CommandProcessor getCommandProcessor() {
		return _commandProcessor;
	}
	
	protected void setCommandProcessor(HtmlCommandProcessor commandProcessor) {
		_commandProcessor = commandProcessor;
	}

	protected boolean runTestSuite(File testSuite, String browser) throws Exception {
		
		getLog().info("Running test suite " + testSuite + " against " + host + ":" + port + " with " + browser);
		
		TestSuite suite = new TestSuite();
		suite.file = testSuite;
		File suiteDirectory = suite.file.getParentFile();
		Document suiteDocument = parseDocument(testSuite);
		Element table = (Element) suiteDocument.getElementsByTagName("table").item(0);
		NodeList tableRows = table.getElementsByTagName("tr");
		Element tableNameRow = (Element) tableRows.item(0);
		suite.name = tableNameRow.getTextContent();
		suite.result = true;
		suite.tests = new TestCase[tableRows.getLength() - 1];
		for (int i = 1; i < tableRows.getLength(); i++) {
			Element tableRow = (Element) tableRows.item(i);
			Element cell = (Element) tableRow.getElementsByTagName("td").item(0);
			Element link = (Element) cell.getElementsByTagName("a").item(0);
			TestCase test = new TestCase();
			test.label = link.getTextContent();
			test.file = new File(suiteDirectory, link.getAttribute("href"));
			runTestCase(test, browser);
			suite.result &= test.result;
			suite.tests[i - 1] = test;
		}
		if (getResultsWriter() != null) {
			getResultsWriter().write("<html><body>");
			getResultsWriter().write("<div>\n");
			getResultsWriter().write("result: " + (suite.result ? "PASSED" : "FAILED") + "\n");
			getResultsWriter().write("<table border=\"1\">\n");
			getResultsWriter().write("<tr><td>" + suite.name + "</td></tr>\n");
			for (TestCase test : suite.tests) {
				getResultsWriter().write("<tr><td>");
				getResultsWriter().write(test.label + "</td><td>" + test.result + "\n");
				getResultsWriter().write("</td></tr>\n");
			}
			getResultsWriter().write("</table>\n");
			getResultsWriter().write("</div>\n");
			for (TestCase test : suite.tests) {
				getResultsWriter().write("<div>\n");
				getResultsWriter().write(test.file.getName() + "\n");
				getResultsWriter().write("<table border=\"1\">\n");
				getResultsWriter().write("<tr><td>" + test.name + "</td></tr>\n");
				for (Command command : test.commands) {
					getResultsWriter().write("<tr><td>");
					getResultsWriter().write(command.cmd);
					getResultsWriter().write("</td><td>");
					if (command.args != null) {
						getResultsWriter().write(Arrays.asList(command.args).toString());
					}
					getResultsWriter().write("</td><td>");
					getResultsWriter().write(command.result);
					getResultsWriter().write("</td></tr>\n");
					if (command.failure) break;
				}
				getResultsWriter().write("</table>\n");
				getResultsWriter().write("</div>\n");
			}
			getResultsWriter().write("</body></html>");
		}
		return suite.result;
	}
	
	public boolean runTestCase(File testCase, String browser) throws Exception {
		TestCase test = new TestCase();
		test.file = testCase;
		Document outputDocument = runTestCase(test, browser);
		
		// Print the DOM node
		if(getResultsWriter() != null) {
			outputDocument(getResultsWriter(), outputDocument);
		}
		return test.result;
	}

	public Document runTestCase(TestCase test, String browser) throws Exception {
		String filename;
		if(testSuite != null) {
			filename = test.file.toString().replaceAll("^" + Pattern.quote(testSuite.getParent().toString()) + "\\/", "");
		}
		else {
			filename = test.file.getName();
		}
		getLog().info("Running test case " + filename + " against " + host + ":" + port + " with " + browser);
		Document document = parseDocument(test.file);

		if (baseUrl == null) {
			NodeList links = document.getElementsByTagName("link");
			if (links.getLength() != 0) {
				Element link = (Element) links.item(0);
				baseUrl = link.getAttribute("href");
			}
		}
		getLog().info("Base URL=" + baseUrl);

		Node body = document.getElementsByTagName("body").item(0);
		Element resultContainer = document.createElement("div");
		resultContainer.setTextContent("Result: ");
		Element resultElt = document.createElement("span");
		resultElt.setAttribute("id", "result");
		resultElt.setIdAttribute("id", true);
		resultContainer.appendChild(resultElt);
		body.insertBefore(resultContainer, body.getFirstChild());

		Element executionLogContainer = document.createElement("div");
		executionLogContainer.setTextContent("Execution Log:");
		Element executionLog = document.createElement("div");
		executionLog.setAttribute("id", "log");
		executionLog.setIdAttribute("id", true);
		executionLog.setAttribute("style", "white-space: pre;");
		executionLogContainer.appendChild(executionLog);
		body.appendChild(executionLogContainer);

		NodeList tableRows = document.getElementsByTagName("tr");
		Node theadRow = tableRows.item(0);
		test.name = theadRow.getTextContent().trim();
		Element stateCell = document.createElement("td");
		stateCell.setTextContent("State");
		theadRow.appendChild(stateCell);
		Element resultCell = document.createElement("td");
		resultCell.setTextContent("Result");
		theadRow.appendChild(resultCell);

		setCommandProcessor(new HtmlCommandProcessor(host, port, browser, baseUrl));
		String resultState;
		String resultLog;
		test.result = true;
		String testName = test.file.getName().replace("\\.html$", "");
		try {
			getLog().info("[" + testName + "] Starting a new session");
			getCommandProcessor().start();
			
			// JS Extensions
			/*
			if(jsExtensions != null) {
				for(File jsExtension : jsExtensions) {
					getLog().info("[" + testName + "] Adding extension " + jsExtension.getName());
					//getCommandProcessor().doCommand("storeEval", new String[]{FileUtils.readFileToString(jsExtension), "none"});
				}
			}
			*/
			
			test.commands = new Command[tableRows.getLength() - 1];
			for (int i = 1; i < tableRows.getLength(); i++) {
				Command command = executeStep((Element) tableRows.item(i), document, testName);
				test.commands[i - 1] = command;
				if (command.failure) {
					test.result = false;
					break;
				}
			}
			resultState = test.result ? "PASSED" : "FAILED";
			resultLog = (test.result ? "Test Complete" : "Error");
			
			getLog().info("[" + testName + "] Ending session");
			getCommandProcessor().stop();
		} catch (Exception e) {
			test.result = false;
			resultState = "ERROR";
			resultLog = "Failed to initialize session\n" + e;
			e.printStackTrace();
		}
		document.getElementById("result").setTextContent(resultState);
		Element log = document.getElementById("log");
		log.setTextContent(log.getTextContent() + resultLog + "\n");
		return document;
	}

	public Command executeStep(Element stepRow, Document document, String testName) throws Exception {
		Command command = new Command();
		NodeList stepFields = stepRow.getElementsByTagName("td");
		String cmd = stepFields.item(0).getTextContent();
		command.cmd = cmd;
		ArrayList<String> argList = new ArrayList<String>();
		if (stepFields.getLength() == 1) {
			// skip comments
			command.result = "OK";
			return command;
		}
		for (int i = 1; i < stepFields.getLength(); i++) {
			String content = stepFields.item(i).getTextContent();
			argList.add(content);
		}
		boolean trimming = true;
		while (trimming && !argList.isEmpty()) {
			int lastIndex = argList.size() - 1;
			String lastArg = argList.get(lastIndex);
			trimming = (lastArg.length() == 0);
			if (trimming) {
				argList.remove(lastIndex);
			}
		};
		for (int i = 0; i < argList.size(); i++) {
			if (argList.get(i).equals("\u00A0")) argList.set(i, "");
		}
		String args[] = argList.toArray(new String[0]);
		command.args = args;
		String result;
		String state;
		boolean passed;
		getLog().info("[" + testName + "] Command " + cmd + " " + Arrays.asList(args));
		try {
			result = getCommandProcessor().doCommand(cmd, args);
			command.result = result;
			state = "OK";
			result = (result.length() > 3) ? result.substring(3) : null;
			passed = true;
		} catch (Exception e) {
			state = "ERROR";
			result = e.getMessage();
			command.result = result;
			passed = false;
		}
		stepRow.appendChild(document.createElement("td")).setTextContent(state);
		if (result != null) {
			stepRow.appendChild(document.createElement("td")).setTextContent(result);
		}
		command.failure = !passed && !cmd.startsWith("verify");
		return command;
	}

	Document parseDocument(File file) throws Exception {
		FileReader reader = new FileReader(file.toString());
		String firstLine = new BufferedReader(reader).readLine();
		Document document = null;
		reader.close();
		if (firstLine.startsWith("<?xml")) {
			System.err.println("XML detected; using default XML parser.");
		} else {
			try {
				Class<?> nekoParserClass = Class.forName("org.cyberneko.html.parsers.DOMParser");
				Object parser = nekoParserClass.newInstance();
				Method parse = nekoParserClass.getMethod("parse", new Class[] { String.class });
				Method getDocument = nekoParserClass.getMethod("getDocument", new Class[0]);
				parse.invoke(parser, file.toString());
				document = (Document) getDocument.invoke(parser);
			} catch (Exception e) {
				System.err.println("NekoHTML HTML parser not found; HTML4 support disabled.");
			}
		}
		if (document == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			// Prevents resolving namespaces (useless)
			builder.setEntityResolver(new EntityResolver() {
				public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					SeleneseMojo.this.getLog().debug("Ignoring: " + publicId + ", " + systemId);
					return new org.xml.sax.InputSource(new java.io.StringReader(""));
				}
			});
			document = builder.parse(file.toString());
		}
		return document;
	}

	void outputDocument(Writer out, Document document) throws Exception {
		
		// Set up the output transformer
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty(OutputKeys.METHOD, "html");

		// Print the DOM node
		StreamResult result = new StreamResult(out);
		DOMSource source = new DOMSource(document);
		trans.transform(source, result);
	}
	
}
