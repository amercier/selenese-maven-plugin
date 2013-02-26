package com.github.amercier.selenium.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;

import com.github.amercier.selenium.ServerAddress;
import com.github.amercier.selenium.exceptions.CapabilitiesNotFoundException;
import com.github.amercier.selenium.exceptions.ElementNotFoundException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.TooManyElementsFoundException;
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
	
	public static int CLOSE_RETRIES = 10;
	
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
	
	protected void recordJavascriptErrors(SeleneseWebDriver driver) {
		driver.executeScript("window.onerror = function(errorMsg, url, lineNumber) { document.body.attributes['data-selenium-error'] = errorMsg + ' in ' + url + ' at line ' + lineNumber }");
	}
	
	protected void checkJavascriptErrors(SeleneseWebDriver driver) {
		Object error = driver.executeScript("return (document.body.attributes['data-selenium-error'])");
		if(error != null) {
			getLog().warn(this + ": (JavaScript error) " + error.toString());
			driver.executeScript("document.body.removeAttribute('data-selenium-error')");
		}
	}
	
	protected File takeScreenShot(SeleneseWebDriver driver) {
		try {
			WebDriver augmentedDriver = new Augmenter().augment(driver);
			File scrFile = ((TakesScreenshot)augmentedDriver).getScreenshotAs(OutputType.FILE);
			File destFile = new File("target/screenshots/" + scrFile.getName());
			FileUtils.copyFile(scrFile, destFile);
			return destFile;
		}
		catch(Throwable e) {
			getLog().error(this + " Error while taking screenshot (" + e.getMessage() + ")");
			return null;
		}
	}
	
	@Override
	public void run() {
		getLog().debug(this + " Starting running test case (" + getTestCase().getCommands().length + " commands)");
		
		boolean closed = false;
		
		if(!isInterrupted()) {
			
			int executedCommands = 0;
			
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
					
					getLog().debug(this + " Running " + command);
					checkJavascriptErrors(driver);
					recordJavascriptErrors(driver);					
					driver.execute(command);
					executedCommands++;
					checkJavascriptErrors(driver);
				}
			}
			
			// Wrongly written tests
			catch(InvalidSeleneseCommandException e) { raiseFailure(e, executedCommands); }
			/*
			catch(UnknownSeleneseCommandException e) { raiseFailure(e, executedCommands); }
			*/
			// Failed test
			catch(AssertionFailedException e)        { raiseError(e, executedCommands, driver); }
			catch(ElementNotFoundException e)        { raiseError(e, executedCommands, driver); }
			catch(TooManyElementsFoundException e)   { raiseError(e, executedCommands, driver); }
			
			// Driver initialization
			catch(CapabilitiesNotFoundException e)   { raiseFailure(e); }
			catch(MalformedURLException e)           { raiseFailure(e); }
			
			// Command execution
			catch(InterruptedException e)            { raiseFailure(e, executedCommands); }
			catch(WebDriverException e)              { raiseFailure(e, executedCommands); }
			catch(RuntimeException e)                { raiseFailure(e, executedCommands); }
			
			finally {
				
				// Close the driver unless its initialization failed
				if(driver != null) {
					
					if(!testCase.hasFailed()) {
						checkJavascriptErrors(driver);
					}
					
					getLog().debug(this + " Closing driver session");
					
					for(int retries = 0 ; !closed && retries < CLOSE_RETRIES ; retries++) {
						try {
							driver.quit();
							closed = true;
						}
						catch(RuntimeException e) {} // do nothing as we throw an Exception later
					}
				}
				else {
					closed = true;
				}
			}
		}
		
		getLog().debug(this + " Finished running test case");
		
		// Free the latch
		try {
			latch.countDown(this);
		}
		catch(RuntimeException e) {
			raiseFailure(e);
		}
		
		// Raise a failure if the driver hasn't been closed properly
		if(!closed) {
			getLog().warn(this + ": failed to close WebDriver session after " + CLOSE_RETRIES + " attempts");
		}
	}
	
	protected URL getServerURL() throws MalformedURLException {
		return new URL("http://" + getServer().getHostName() + ":" + getServer().getPort() + REMOTE_SERVER_PATH);
	}
	
	protected SeleneseWebDriver initWebDriver() throws MalformedURLException, CapabilitiesNotFoundException {
		try {
			final Log log = getLog();
			return new SeleneseWebDriver(getBaseUrl(), getServerURL(), getCapability().toCapabilities(), new com.github.amercier.selenium.selenese.log.Log() {
				public void warn (String message) { log.warn (TestCaseRunner.this + " " + message); }
				public void info (String message) { log.info (TestCaseRunner.this + " " + message); }
				public void error(String message) { log.error(TestCaseRunner.this + " " + message); }
				public void debug(String message) { log.debug(TestCaseRunner.this + " " + message); }
			});
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
	
	protected StackTraceElement[] getSeleneseStackTrace(int executedCommands) {
		SeleneseCommand[] commands = getTestCase().getCommands();
		StackTraceElement[] stacktrace = new StackTraceElement[Math.min(executedCommands + 1, commands.length)];
		for(int i = 0 ; i < Math.min(executedCommands + 1, commands.length) ; i++) {
			stacktrace[i] = new StackTraceElement("", commands[i].toString(), "", i);
		}
		return stacktrace;
	}
	
	protected void raiseError(Throwable failure, int executedCommands, SeleneseWebDriver driver) {
		SeleneseCommand[] commands = getTestCase().getCommands();
		SeleneseCommand command = executedCommands == commands.length ? null : commands[executedCommands];
		failure.setStackTrace(getSeleneseStackTrace(executedCommands));
		File screenshot = takeScreenShot(driver);
		this.setError(new MojoExecutionException((command == null ? "test case shutdown " : command + ": ") + (failure.getMessage().equals("") ? "unknown " + failure.getClass().getName() + " error" : failure.getMessage()) + (screenshot == null ? "" : ". A screenshot has been recorded as " + screenshot.getName()), failure));
	}
	
	protected void raiseFailure(Throwable failure) {
		this.setFailure(new MojoFailureException(failure.getMessage(), failure));
	}
	
	protected void raiseFailure(Throwable failure, int executedCommands) {
		SeleneseCommand[] commands = getTestCase().getCommands();
		SeleneseCommand command = executedCommands == commands.length ? null : commands[executedCommands];
		failure.setStackTrace(getSeleneseStackTrace(executedCommands));
		this.setFailure(new MojoFailureException((command == null ? "test case shutdown " : command + ": ") + (failure.getMessage().equals("") ? "unknown " + failure.getClass().getName() + " error" : failure.getMessage()), failure));
	}
	
	@Override
	public String toString() {
		return "Test case [" + getTestCase().getName() + " @ " + getCapability() + "]";
	}
}
