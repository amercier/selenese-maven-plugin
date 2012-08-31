package com.github.amercier.selenium.maven;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;
import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;
import com.github.amercier.selenium.selenese.SeleneseCommand;
import com.github.amercier.selenium.selenese.SeleneseCommandInterpreter;
import com.github.amercier.selenium.selenese.SeleneseTestCase;
import com.github.amercier.selenium.thread.ObservableCountDownLatch;

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
	 * The URL of the web site to be tested
	 */
	protected URL baseUrl;
	
	/**
	 * The test failure. If null, test has succeeded
	 */
	protected Throwable failure;
	
	/**
	 * Synchronization manager
	 */
	protected ObservableCountDownLatch<TestCaseRunner> latch;
	
	/**
	 * Log
	 */
	protected Log log; 
	
	/**
	 * Create a test case runner
	 */
	public TestCaseRunner(InetSocketAddress server, SeleneseTestCase testCase, DesiredCapabilities capability, URL baseUrl, ObservableCountDownLatch<TestCaseRunner> latch, Log log) {
		setServer(server);
		setTestCase(testCase);
		setCapability(capability);
		setBaseUrl(baseUrl);
		setLatch(latch);
		setLog(log);
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
	
	public URL getBaseUrl() {
		return baseUrl;
	}
	
	public void setBaseUrl(URL baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public boolean hasSucceeded() {
		return this.getFailure() == null;
	}
	
	public boolean hasFailed() {
		return this.getFailure() != null;
	}
	
	public Throwable getFailure() {
		return failure;
	}
	
	protected void setFailure(Throwable failure) {
		this.failure = failure;
	}
	
	protected ObservableCountDownLatch<TestCaseRunner> getLatch() {
		return latch;
	}
	
	protected void setLatch(ObservableCountDownLatch<TestCaseRunner> latch) {
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
		getLog().debug(this + " Starting running test case (" + getTestCase().getCommands().length + " commands)");
		if(!isInterrupted()) {
			
			WebDriver driver = null;
			try {
				
				// Driver & interpreter initialization
				driver = initWebDriver();
				getLog().debug(this + " Initialized web driver successfully");
				SeleneseCommandInterpreter intepreter = initCommandDriver(driver);
				getLog().debug(this + " Initialized interpreter successfully");
				
				// Run commands
				for(SeleneseCommand command : getTestCase().getCommands()) {
					if(isInterrupted()) {
						break;
					}
					getLog().debug(this + " Running " + command);
					intepreter.execute(command);
				}
			}
			catch(MalformedURLException e) {
				getLog().debug(e);
				this.setFailure(e);
			}
			catch(UnknownSeleneseCommandException e) {
				getLog().debug(e);
				this.setFailure(e);
			}
			catch(WebDriverException e) {
				getLog().debug(e);
				this.setFailure(e);
			}
			catch(RuntimeException e) {
				getLog().debug(e);
				this.setFailure(e);
			}
			finally {
				// Close the driver unless its initialization failed
				if(driver != null) {
					getLog().debug(this + " Closing driver session");
					driver.close();
				}
			}
		}
		
		getLog().debug(this + " Finished running test case");
		latch.countDown(this);
	}
	
	protected URL getRemoteURL() throws MalformedURLException {
		return new URL("http://" + getServer().getHostName() + ":" + getServer().getPort() + REMOTE_SERVER_PATH);
	}
	
	protected WebDriver initWebDriver() throws MalformedURLException {
		return new RemoteWebDriver(getRemoteURL(), getCapability().toCapabilities());
	}
	
	protected SeleneseCommandInterpreter initCommandDriver(WebDriver driver) {
		return new SeleneseCommandInterpreter(driver, this.getBaseUrl());
	}
	
	@Override
	public String toString() {
		return "[" + getTestCase().getName() + " @ " + getCapability() + "]";
	}
}
