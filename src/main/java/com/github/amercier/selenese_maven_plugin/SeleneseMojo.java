package com.github.amercier.selenese_maven_plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
	 * @required
	 */
	public File testSuite;
	
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
	public String browser = "*firefox";
	
	/**
	 * The directory to write the test results reports
	 * 
	 * @parameter expression="${selenium.resultsDir}"
	 */
	File resultsDir;
	
	/**
	 * Run the tests
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug("Running test suite " + testSuite + " against " + host + ":" + port + " with " + browser);
	}
}
