package com.github.amercier.selenium.maven;

import java.util.concurrent.CountDownLatch;

import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;
import com.github.amercier.selenium.selenese.SeleneseCommand;
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
	 * Synchronization manager
	 */
	protected CountDownLatch latch;
	
	/**
	 * Create a test case runner
	 */
	public TestCaseRunner(SeleneseTestCase testCase, DesiredCapabilities capability, CountDownLatch latch) {
		this.setTestCase(testCase);
		this.setCapability(capability);
		this.setResult(null);
		this.latch = latch;
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
		System.out.println("Starting running test case " + getTestCase().getName() + " (" + getTestCase().getCommands().length + " commands) on " + getCapability());
		if(!isInterrupted()) {
			try {
				for(SeleneseCommand command : getTestCase().getCommands()) {
					if(isInterrupted()) {
						break;
					}
					System.out.println("[" + getTestCase().getName() + " @ " + getCapability() + "] Running " + command);
					Thread.sleep((long) (1000 * Math.random()));
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finished running test case " + getTestCase().getName() + " on " + getCapability());
		latch.countDown();
	}
}
