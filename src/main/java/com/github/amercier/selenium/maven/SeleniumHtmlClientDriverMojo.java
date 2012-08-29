package com.github.amercier.selenium.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;
import com.github.amercier.selenium.selenese.SeleneseTestCase;
import com.github.amercier.selenium.selenese.document.TestCaseDocument;
import com.github.amercier.selenium.selenese.document.TestSuiteDocument;

/**
 * Goal which sends Selenese HTML tests to be run by a remote or local Selenium
 * 2 Server/Grid Hub
 * 
 * @goal  test
 * @phase test
 */
public class SeleniumHtmlClientDriverMojo extends AbstractMojo {
	
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
	 * Desired capabilities
	 * @parameter expression="${selenium.desiredCapabilities}"
	 */
	public DesiredCapabilities[] capabilities = new DesiredCapabilities[] { };
	
	/**
	 * The file to write the test results reports
	 * 
	 * @parameter expression="${selenium.resultsFile}"
	 * @required
	 */
	public File resultsFile;
	
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
			
			// Run either the test case (if specified) or the test suite
			if(testSuite != null) {
				for(DesiredCapabilities browserConfig : capabilities) {
					runTestSuite(new TestSuiteDocument(testSuite), browserConfig);
				}
			}
			else {
				for(DesiredCapabilities browserConfig : capabilities) {
					runTestCase(new TestCaseDocument(testCase), browserConfig);
				}
			}
		}
		catch(Exception e) {
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
	
	/**
	 * Ru a test suite
	 * @param testSuiteDocument
	 * @param capability
	 * @throws IOException 
	 * @throws SAXException 
	 */
	protected void runTestSuite(TestSuiteDocument testSuiteDocument, DesiredCapabilities capability) throws SAXException, IOException {
		getLog().info("Running test suite " + testSuiteDocument.getSourceFile().getName() + " on config " + capability);
		for(TestCaseDocument testCaseDocument : testSuiteDocument.getTestCaseDocuments()) {
			runTestCase(testCaseDocument, capability);
		}
	}

	/**
	 * Run a test case
	 * @param testCaseDocument
	 * @param capability
	 */
	protected void runTestCase(TestCaseDocument testCaseDocument, DesiredCapabilities capability) {
		getLog().info("Running test case " + testCaseDocument.getSourceFile().getName() + " on config " + capability);
		for(SeleneseTestCase testCase : testCaseDocument.getTestCases()) {
			new TestCaseRunner(testCase, capability.toCapabilities()).run();
		}
	}
}
