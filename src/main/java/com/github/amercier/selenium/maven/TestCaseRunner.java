package com.github.amercier.selenium.maven;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.github.amercier.selenium.exceptions.TestCaseFailedException;
import com.github.amercier.selenium.selenese.SeleneseTestCase;

/**
 * A thread responsible of running a test case and report
 */
public class TestCaseRunner extends Thread {
	
	/**
	 * The test case to run
	 */
	protected SeleneseTestCase testCase;
	
	/**
	 * The client configuration to be run on
	 */
	protected DesiredCapabilities capability;
	
	/**
	 * The test result.
	 * Null if not run yet, true if succeeded, false if failed
	 */
	protected Boolean result;
	
	/**
	 * Create a test case runner
	 */
	public TestCaseRunner(SeleneseTestCase testCase, DesiredCapabilities capability) {
		this.setTestCase(testCase);
		this.setCapability(capability);
		this.setResult(null);
	}
	
	public SeleneseTestCase getTestCase() {
		return testCase;
	}
	
	public void setTestCase(SeleneseTestCase testCase) {
		this.testCase = testCase;
	}
	
	public DesiredCapabilities getCapability() {
		return capability;
	}
	
	public void setCapability(DesiredCapabilities capability) {
		this.capability = capability;
	}
	
	public Boolean getResult() {
		return result;
	}
	
	public void setResult(Boolean result) {
		this.result = result;
	}

	@Override
	public void run() {
		while(!isInterrupted()) {
			try {
				
			}
			catch(Exception e) {
				
			}
		}
	}
	
	protected void runTestCase() throws TestCaseFailedException {
		
	}
}
