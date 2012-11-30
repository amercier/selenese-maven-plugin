package com.github.amercier.selenium.maven;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import net.jsourcerer.webdriver.jserrorcollector.JavaScriptError;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.github.amercier.selenium.ServerAddress;
import com.github.amercier.selenium.exceptions.CapabilitiesNotFoundException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;
import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;
import com.github.amercier.selenium.selenese.SeleneseCommand;
import com.github.amercier.selenium.selenese.SeleneseTestCase;
import com.github.amercier.selenium.selenese.SeleneseWebDriver;
import com.github.amercier.selenium.selenese.assertions.AssertionFailedException;
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
	 * Remote session path
	 */
	public static String REMOTE_SESSION_PATH = "/grid/api/testsession?session=";
	
	/**
	 * The remote server / grid hub address
	 */
	protected ServerAddress server;
	
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
	 * Synchronization manager
	 */
	protected ObservableCountDownLatch<TestCaseRunner> latch;
	
	/**
	 * Log
	 */
	protected Log log;
	
	/**
	 * JUnit Test Case
	 */
	protected junit.framework.TestCase jUnitTestCase;
	
	/**
	 * Create a test case runner
	 */
	public TestCaseRunner(ServerAddress server, SeleneseTestCase testCase, DesiredCapabilities capability, URL baseUrl, ObservableCountDownLatch<TestCaseRunner> latch, Log log) {
		setServer(server);
		setTestCase(testCase);
		setCapability(capability);
		setBaseUrl(baseUrl);
		setLatch(latch);
		setLog(log);
		setJUnitTestCase(new junit.framework.TestCase(toString()){});
	}
	
	public ServerAddress getServer() {
		return server;
	}
	
	public void setServer(ServerAddress server) {
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
	
	public junit.framework.TestCase getJUnitTestCase() {
		return jUnitTestCase;
	}
	
	public void setJUnitTestCase(junit.framework.TestCase jUnitTestCase) {
		this.jUnitTestCase = jUnitTestCase;
	}
	
	protected void setError(MojoExecutionException error) {
		this.getTestCase().setError(error);
	}
	
	protected void setFailure(MojoFailureException failure) {
		this.getTestCase().setFailure(failure);
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
			
			SeleneseCommand currentCommand = null;
			
			SeleneseWebDriver driver = null;
			try {
				
				// Driver & interpreter initialization
				driver = initWebDriver();
				getLog().info(this + " Starting on " + getNodeName(driver));
				
				// Run commands
				for(SeleneseCommand command : getTestCase().getCommands()) {
					if(isInterrupted()) {
						break;
					}
					currentCommand = command;
					getLog().debug(this + " Running " + command);
					driver.execute(command);
				}
			}
			
			// Driver initialization
			catch(CapabilitiesNotFoundException e)   { raiseError(e); }
			catch(MalformedURLException e)           { raiseFailure(e); }
			
			// Command execution
			catch(AssertionFailedException e)        { raiseError(e, currentCommand, driver); }
			catch(InvalidSeleneseCommandException e) { raiseFailure(e); }
			catch(UnknownSeleneseCommandException e) { raiseFailure(e); }
			catch(InterruptedException e)            { raiseFailure(e); }
			catch(WebDriverException e)              { raiseError(e, currentCommand, driver); }
			
			// Other
			catch(RuntimeException e)                { raiseFailure(e); }
			
			finally {
				// Close the driver unless its initialization failed
				if(driver != null) {
					getLog().debug(this + " Closing driver session");
					try {
						driver.quit();
					}
					catch(RuntimeException e) { raiseFailure(e); }
				}
			}
		}
		
		getLog().debug(this + " Finished running test case");
		
		try {
			latch.countDown(this);
		}
		catch(RuntimeException e) { raiseFailure(e); }
	}
	
	protected URL getServerURL() throws MalformedURLException {
		return new URL("http://" + getServer().getHostName() + ":" + getServer().getPort() + REMOTE_SERVER_PATH);
	}
	
	protected SeleneseWebDriver initWebDriver() throws MalformedURLException, CapabilitiesNotFoundException {
		try {
			return new SeleneseWebDriver(getBaseUrl(), getServerURL(), getCapability().toCapabilities());
		}
		catch(WebDriverException e) {
			throw new CapabilitiesNotFoundException(getCapability(), getServer());
		}
	}
	
	public String getNodeName(SeleneseWebDriver driver) {
		try {
			URL url = new URL("http://" + getServer().getHostName() + ":" + getServer().getPort() + REMOTE_SESSION_PATH + driver.getSessionId().toString());
			URLConnection connection = url.openConnection();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String rawResponse = "",
			       rawResponseLine;
			while ((rawResponseLine = buffer.readLine()) != null) {
				rawResponse += (rawResponse == "" ? "" : "\n") + rawResponseLine;
			}
			buffer.close();
			JSONObject response = new JSONObject(rawResponse);
			if(response.getBoolean("success")) {
				return response.getString("proxyId").replaceAll("^http:\\/\\/(.*):[0-9]+$", "$1");
			}
			else {
				return response.getString("msg");
			}
		}
		catch(Exception e) {
			return "Error: " + e.getMessage();
		}
	}
	
	protected void raiseError(Throwable failure) {
		this.setError(new MojoExecutionException(failure.getMessage(), failure));
	}
	
	protected void raiseError(Throwable failure, SeleneseCommand command, WebDriver driver) {
		
		// Add a Javascript errors stacktrace
		String stacktrace = "";
		if(failure instanceof AssertionFailedException) {
			List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
			if(jsErrors.isEmpty()) {
				stacktrace = "\nNo JavaScript errors";
			}
			else {
				stacktrace = jsErrors.toString();
			}
		}
		
		this.setError(new MojoExecutionException(command + ": " + failure.getMessage() + stacktrace, failure));
	}
	
	protected void raiseFailure(Throwable failure) {
		this.setFailure(new MojoFailureException(failure.getMessage(), failure));
	}
	
	@Override
	public String toString() {
		return "Test case [" + getTestCase().getName() + " @ " + getCapability() + "]";
	}
}
