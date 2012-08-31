package com.github.amercier.selenium.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.ServerAddress;
import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;
import com.github.amercier.selenium.selenese.InvalidSeleneseCommandNameException;
import com.github.amercier.selenium.selenese.SeleneseTestCase;
import com.github.amercier.selenium.selenese.SeleneseTestSuite;
import com.github.amercier.selenium.selenese.document.TestCaseDocument;
import com.github.amercier.selenium.selenese.document.TestSuiteDocument;
import com.github.amercier.selenium.selenese.log.Log;
import com.github.amercier.selenium.thread.CountDownLatchListener;
import com.github.amercier.selenium.thread.ObservableCountDownLatch;

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
	public URL baseUrl;
	
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
		
		ObservableCountDownLatch<TestCaseRunner> latch = null;
		
		// Create a CountDownLatch listener that logs about test runner individual results
		CountDownLatchListener<TestCaseRunner> listener = new CountDownLatchListener<TestCaseRunner>() {
			public void fireCountedDown(ObservableCountDownLatch<TestCaseRunner> observableCountDownLatch, TestCaseRunner terminated) {
				if(terminated.hasSucceeded()) {
					SeleniumHtmlClientDriverMojo.this.getLog().info(terminated + " Succeeded");
				}
				else {
					SeleniumHtmlClientDriverMojo.this.getLog().error(terminated + " Failed (" + terminated.getFailure().getMessage().replaceAll("\\n.*", "") + ")");
				}
			}
		};
		
		List<TestCaseRunner> testRunners = new LinkedList<TestCaseRunner>();
		
		// Create a Selenese-to-Maven log bridge
		Log seleneseLog = new Log() {
			public void debug(String message) { SeleniumHtmlClientDriverMojo.this.getLog().debug(message); }
			public void info(String message)  { SeleniumHtmlClientDriverMojo.this.getLog().info(message);  }
			public void warn(String message)  { SeleniumHtmlClientDriverMojo.this.getLog().warn(message);  }
			public void error(String message) { SeleniumHtmlClientDriverMojo.this.getLog().error(message); }
		};
		
		try {
			
			// Check testCase XOR testSuite
			if (testCase == null && testSuite == null) {
				throw new RuntimeException("No testCase or testSuite file specified");
			}
			else if (testCase != null && testSuite != null) {
				throw new RuntimeException("A testCase and testSuite file cannot both be specified");
			}
			
			ServerAddress server = new ServerAddress(host, port);
			
			// Run either the test case (if specified) or the test suite
			if(testSuite != null) {
				getLog().info("Reading test suite " + testSuite.getName());
				SeleneseTestSuite suite = new TestSuiteDocument(testSuite, seleneseLog).getTestSuite();
				
				latch = new ObservableCountDownLatch<TestCaseRunner>(suite.getTestCases().length * capabilities.length);
				latch.addListener(listener);
				
				getLog().info(suite.getTestCases().length + " test case(s) will be run on " + capabilities.length + " configuration(s) (total: " + suite.getTestCases().length * capabilities.length + ")");
				
				for(DesiredCapabilities capability : capabilities) {
					getLog().debug("Running test suite " + testSuite.getName() + " on config " + capability);
					for(SeleneseTestCase testCase : suite.getTestCases()) {
						getLog().debug("Running test case " + testCase.getName() + " on config " + capability);
						TestCaseRunner testRunner = new TestCaseRunner(server, testCase, capability, baseUrl, latch, getLog());
						testRunners.add(testRunner);
						testRunner.start();
					}
				}
			}
			else {
				getLog().info("Reading test case " + testCase.getName());
				SeleneseTestCase testCase = new TestCaseDocument(testSuite, seleneseLog).getTestCase();
				
				latch = new ObservableCountDownLatch<TestCaseRunner>(capabilities.length);
				latch.addListener(listener);
				
				getLog().info("The test case will be run on " + capabilities.length + " configuration(s)");
				
				for(DesiredCapabilities capability : capabilities) {
					getLog().debug("Running test case " + testCase.getName() + " on config " + capability);
					TestCaseRunner testRunner = new TestCaseRunner(server, testCase, capability, baseUrl, latch, getLog());
					testRunners.add(testRunner);
					testRunner.start();
				}
			}
		}
		catch (DOMException e)                        { throw new MojoFailureException(e.getMessage(), e); }
		catch (InvalidSeleneseCommandNameException e) { throw new MojoFailureException(e.getMessage(), e); }
		catch (SAXException e)                        { throw new MojoFailureException(e.getMessage(), e); }
		catch (IOException e)                         { throw new MojoFailureException(e.getMessage(), e); }
		catch(RuntimeException e)                     { throw new MojoFailureException(e.getMessage(), e); }
		
		// Wait for all test runners to terminate
		if(latch != null) {
			try {
				getLog().debug("Waiting for " + latch.getCount() + " test runner(s) to finish");
				latch.await();
			}
			catch (InterruptedException e) {
				throw new MojoFailureException(e.getMessage(), e);
			}
		}
		getLog().debug("All test runners have been terminated");
		
		for(TestCaseRunner testRunner : testRunners) {
			if(testRunner.hasFailed()) {
				if(testRunner.getFailure() instanceof MojoFailureException) {
					throw (MojoFailureException)testRunner.getFailure();
				}
				else if(testRunner.getFailure() instanceof MojoExecutionException) {
					throw (MojoExecutionException)testRunner.getFailure();
				}
			}
		}
	}
}
