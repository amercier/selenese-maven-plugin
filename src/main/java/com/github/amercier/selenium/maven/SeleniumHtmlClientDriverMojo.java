package com.github.amercier.selenium.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.github.amercier.selenium.ServerAddress;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;
import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;
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
 * @todo Parameter timeout = 30000
 * @todo Parameter maxSimultaneousTests = 0
 * @todo Parameter delayBetweenTests = 0
 * 
 * @goal  integration-test
 * @phase integration-test
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
	 * Delay between two consecutive commands
	 * 
	 * @parameter expression="${selenium.commandInterval}"
	 */
	public long commandInterval = 100;
	
	
	/**
	 * Delay between two consecutive commands
	 * 
	 * @parameter expression="${selenium.startInterval}"
	 */
	public long startInterval = 1000;
	
	
	/**
	 * Timeout for waitFor commands
	 * 
	 * @parameter expression="${selenium.waitTimeout}"
	 */
	public long waitTimeout = 30 * 1000; // 30 seconds
	
	
	/**
	 * Report formatter
	 */
	XMLJUnitResultFormatter formatter;
	
	/**
	 * Run the tests
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		ObservableCountDownLatch<TestCaseRunner> latch = null;
		FileOutputStream outputStream = null;
		formatter = new XMLJUnitResultFormatter();
		try {
			outputStream = FileUtils.openOutputStream(resultsFile);
		}
		catch (IOException e) {
			throw new MojoFailureException("Can't create output file", e);
		}
		formatter.setOutput(outputStream);
		
		// Create a CountDownLatch listener that logs about test runner individual results
		CountDownLatchListener<TestCaseRunner> testCaseLogger = new CountDownLatchListener<TestCaseRunner>() {
			public void fireCountedDown(ObservableCountDownLatch<TestCaseRunner> observableCountDownLatch, TestCaseRunner terminated) {
				SeleneseTestCase testCase = terminated.getTestCase();
				if(testCase.hasSucceeded()) {
					SeleniumHtmlClientDriverMojo.this.getLog().info(terminated + " SUCCESS");
				}
				else if(testCase.hasErrored()) {
					SeleniumHtmlClientDriverMojo.this.getLog().error(terminated + " ERROR " + terminated.getTestCase().getError().getMessage().replaceAll("\\n.*", ""));
				}
				else {
					SeleniumHtmlClientDriverMojo.this.getLog().error(terminated + " FAILURE " + terminated.getTestCase().getFailure().getMessage().replaceAll("\\n.*", ""));
				}
			}
		};
		
		// Create a CountDownLatch listener that update status of the 
		CountDownLatchListener<TestCaseRunner> testReportUpdater = new CountDownLatchListener<TestCaseRunner>() {
			public void fireCountedDown(ObservableCountDownLatch<TestCaseRunner> observableCountDownLatch, TestCaseRunner terminated) {
				SeleneseTestCase testCase = terminated.getTestCase();
				if(testCase.hasFailed()) {
					formatter.addFailure(terminated.getJUnitTestCase(), testCase.getFailure());
				}
				else if(testCase.hasErrored()) {
					formatter.addError(terminated.getJUnitTestCase(), testCase.getError());
				}
				formatter.endTest(terminated.getJUnitTestCase());
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
		
		final SeleneseTestSuite suite;
		try {
			
			// Check testCase XOR testSuite
			if (testCase == null && testSuite == null) {
				throw new RuntimeException("No testCase or testSuite file specified");
			}
			else if (testCase != null && testSuite != null) {
				throw new RuntimeException("A testCase and testSuite file cannot both be specified");
			}
			
			ServerAddress server = new ServerAddress(host, port);
			
			suite = testSuite == null ? null : new TestSuiteDocument(testSuite, seleneseLog).getTestSuite();
			
			// Run either the test case (if specified) or the test suite
			if(testSuite != null) {
				getLog().debug("Reading test suite " + testSuite.getName());
				formatter.startTestSuite(suite);
				latch = new ObservableCountDownLatch<TestCaseRunner>(suite.getTestCases().length * capabilities.length);
				latch.addListener(testCaseLogger);
				latch.addListener(testReportUpdater);
				latch.addListener(new CountDownLatchListener<TestCaseRunner>() {
					public void fireCountedDown(ObservableCountDownLatch<TestCaseRunner> latch, TestCaseRunner item) {
						SeleneseTestCase testCase = item.getTestCase();
						if(testCase.hasFailed()) {
							suite.setCounts(suite.runCount(), suite.failureCount() + 1, suite.errorCount());
						}
						else if(testCase.hasErrored()) {
							suite.setCounts(suite.runCount(), suite.failureCount(), suite.errorCount() + 1);
						}
					}
				});
				
				int testCasesCount = suite.getTestCases().length * capabilities.length;
				getLog().info("Running " + testCasesCount + " test case" + (testCasesCount > 1 ? "s" : "") + " (" + suite.getTestCases().length + " test case" + (suite.getTestCases().length > 1 ? "s" : "") + " on " + capabilities.length + " configuration" + (capabilities.length > 1 ? "s" : "") + ") against " + baseUrl);
				
				for(DesiredCapabilities capability : capabilities) {
					getLog().debug("Running test suite " + testSuite.getName() + " on config " + capability);
					boolean first = true;
					for(SeleneseTestCase testCase : suite.getTestCases()) {
						if(first) {
							first = false;
						}
						else {
							Thread.sleep(startInterval);
						}
						getLog().debug("Running test case " + testCase.getName() + " on config " + capability);
						TestCaseRunner testRunner = new TestCaseRunner(server, testCase, capability, baseUrl, latch, getLog(), commandInterval, waitTimeout);
						formatter.startTest(testRunner.getJUnitTestCase());
						testRunners.add(testRunner);
						testRunner.start();
						suite.setCounts(suite.runCount() + 1, suite.failureCount(), suite.errorCount());
					}
				}
			}
			else {
				getLog().debug("Reading test case " + testCase.getName());
				SeleneseTestCase testCase = new TestCaseDocument(testSuite, seleneseLog).getTestCase();
				
				latch = new ObservableCountDownLatch<TestCaseRunner>(capabilities.length);
				latch.addListener(testCaseLogger);
				latch.addListener(testReportUpdater);
				
				getLog().info("Running " + capabilities.length + " test case" + (capabilities.length > 1 ? "s" : "") + " (1 test case on " + capabilities.length + " configuration" + (capabilities.length > 1 ? "s" : "") + ") against " + baseUrl);
				
				boolean first = true;
				for(DesiredCapabilities capability : capabilities) {
					if(first) {
						first = false;
					}
					else {
						Thread.sleep(startInterval);
					}
					getLog().debug("Running test case " + testCase.getName() + " on config " + capability);
					TestCaseRunner testRunner = new TestCaseRunner(server, testCase, capability, baseUrl, latch, getLog(), commandInterval, waitTimeout);
					testRunners.add(testRunner);
					testRunner.start();
				}
			}
		}
		catch (InterruptedException e)            {	throw new MojoFailureException(e.getMessage(), e); }
		catch (DOMException e)                    { throw new MojoFailureException(e.getMessage(), e); }
		catch (SAXException e)                    { throw new MojoFailureException(e.getMessage(), e); }
		catch (IOException e)                     { throw new MojoFailureException(e.getMessage(), e); }
		catch (UnknownSeleneseCommandException e) { throw new MojoFailureException(e.getMessage(), e); }
		catch (InvalidSeleneseCommandException e) { throw new MojoFailureException(e.getMessage(), e); }
		catch (RuntimeException e)                { throw new MojoFailureException(e.getMessage(), e); }
		
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
		
		
		// Close suite, if any
		if(suite != null) {
			formatter.endTestSuite(suite);
		}
		
		// Close output stream
		try {
			outputStream.close();
		}
		catch (IOException e) {
			throw new MojoFailureException("Can't close output file", e);
		}
		
		// Throw a Mojo Exception on the first failed test runner
		for(TestCaseRunner testRunner : testRunners) {
			SeleneseTestCase testCase = testRunner.getTestCase();
			if(testCase.hasFailed()) {
				throw (MojoFailureException)testCase.getFailure();
			}
			else if(testCase.hasErrored()) {
				getLog().error(testCase.getError());
			}
		}
	}
}
