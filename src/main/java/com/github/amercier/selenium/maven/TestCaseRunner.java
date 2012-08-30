package com.github.amercier.selenium.maven;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import org.apache.maven.plugin.logging.Log;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;
import com.github.amercier.selenium.selenese.SeleneseCommand;
import com.github.amercier.selenium.selenese.SeleneseCommandInterpreter;
import com.github.amercier.selenium.selenese.SeleneseTestCase;

/**
 * A thread responsible of running a test case and report
 */
public class TestCaseRunner extends Thread {
	
	/**
	 * Remote server path
	 */
	public static String REMOTE_SERVER_PATH = "/wd/hub";
	
	/**
	 * The remote server / grid hub address
	 */
	protected InetSocketAddress server;
	
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
	 * Log
	 */
	protected Log log; 
	
	/**
	 * Create a test case runner
	 */
	public TestCaseRunner(InetSocketAddress server, SeleneseTestCase testCase, DesiredCapabilities capability, CountDownLatch latch, Log log) {
		this.setTestCase(testCase);
		this.setCapability(capability);
		this.setResult(null);
		this.setLatch(latch);
		this.setLog(log);
	}
	
	public InetSocketAddress getServer() {
		return server;
	}
	
	public void setServer(InetSocketAddress server) {
		this.server = server;
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
	
	protected CountDownLatch getLatch() {
		return latch;
	}
	
	protected void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
	
	protected Log getLog() {
		return log;
	}
	
	protected void setLog(Log log) {
		this.log = log;
	}
	
	@Override
	public void run() {
		System.out.println("Starting running test case " + getTestCase().getName() + " (" + getTestCase().getCommands().length + " commands) on " + getCapability());
		if(!isInterrupted()) {
			try {
				
				// Driver & interpreter initialization
				WebDriver driver = initWebDriver();
				SeleneseCommandInterpreter intepreter = initCommandDriver(driver);
				
				// Run commands
				for(SeleneseCommand command : getTestCase().getCommands()) {
					if(isInterrupted()) {
						break;
					}
					System.out.println("[" + getTestCase().getName() + " @ " + getCapability() + "] Running " + command);
					intepreter.execute(command);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finished running test case " + getTestCase().getName() + " on " + getCapability());
		latch.countDown();
	}
	
	protected URL getRemoteURL() throws MalformedURLException {
		return new URL("http://" + getServer().getHostName() + ":" + getServer().getPort() + REMOTE_SERVER_PATH);
	}
	
	protected WebDriver initWebDriver() throws MalformedURLException {
		return new RemoteWebDriver(getRemoteURL(), getCapability().toCapabilities());
	}
	
	protected SeleneseCommandInterpreter initCommandDriver(WebDriver driver) {
		return new SeleneseCommandInterpreter(driver);
	}
}
