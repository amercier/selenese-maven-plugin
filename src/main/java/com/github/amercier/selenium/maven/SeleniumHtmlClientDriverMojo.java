package com.github.amercier.selenium.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
			if(testCase != null) {
				for(DesiredCapabilities browserConfig : capabilities) {
					runTestCase(testCase, browserConfig);
				}
			}
			else {
				for(DesiredCapabilities browserConfig : capabilities) {
					runTestSuite(testSuite, browserConfig);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	protected void runTestSuite(File testSuite, DesiredCapabilities browserConfig) {
		getLog().info("Running test suite " + testSuite.getName() + " on browser " + browserConfig);
	}

	protected void runTestCase(File testCase2, DesiredCapabilities browserConfig) {
		getLog().info("Running test case " + testSuite.getName() + " on browser " + browserConfig);
	}
}
