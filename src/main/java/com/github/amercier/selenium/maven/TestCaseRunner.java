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
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.XPathLookupException;
import org.openqa.selenium.interactions.InvalidCoordinatesException;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.JsonException;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import com.github.amercier.selenium.ServerAddress;
import com.github.amercier.selenium.exceptions.CapabilitiesNotFoundException;
import com.github.amercier.selenium.exceptions.ElementNotFoundException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.SeleniumNodeNameException;
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
	 * Delay between two consecutive commands
	 */
	protected long commandInterval;
	
	/**
	 * Pause before the first command
	 */
	protected long startDelay;
	
	/**
	 * WaitFor commands Timeout
	 */
	protected long waitTimeout;
	
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
	public TestCaseRunner(ServerAddress server, SeleneseTestCase testCase, DesiredCapabilities capability, URL baseUrl, ObservableCountDownLatch<TestCaseRunner> latch, Log log, long commandInterval, long startDelay, long waitTimeout) {
		setServer(server);
		setTestCase(testCase);
		setCapability(capability);
		setBaseUrl(baseUrl);
		setLatch(latch);
		setLog(log);
		setCommandInterval(commandInterval);
		setStartDelay(startDelay);
		setWaitTimeout(waitTimeout);
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
	
	public long getCommandInterval() {
		return commandInterval;
	}
	
	public void setCommandInterval(long commandInterval) {
		this.commandInterval = commandInterval;
	}
	
	public long getStartDelay() {
		return startDelay;
	}
	
	public void setStartDelay(long startDelay) {
		this.startDelay = startDelay;
	}
	
	public long getWaitTimeout() {
		return waitTimeout;
	}
	
	public void setWaitTimeout(long waitTimeout) {
		this.waitTimeout = waitTimeout;
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

				// Run the startDelay sleep
				Thread.sleep(getStartDelay());

				getLog().info(this + " Starting on " + getNodeName(driver));
				

				// Run a random sleep to un-sync the runners
				if(getCommandInterval() > 0) {
					Thread.sleep((long) (Math.random() * getCommandInterval()));
				}
				
				// Run commands
				for(SeleneseCommand command : getTestCase().getCommands()) {
					
					if(isInterrupted()) {
						break;
					}
					
					if(getCommandInterval() != 0) {
						Thread.sleep(getCommandInterval());
					}
					
					getLog().debug(this + " Running " + command);
					recordJavascriptErrors(driver);					
					driver.execute(command);
					executedCommands++;
					checkJavascriptErrors(driver);
				}
			}
			
			// Wrongly written tests
			catch(InvalidSeleneseCommandException e) { raiseFailure(e, executedCommands); }
			
			// Failed test
			catch(AssertionFailedException e)        { raiseError(e, executedCommands, driver); }
			catch(ElementNotFoundException e)        { raiseError(e, executedCommands, driver); }
			catch(ElementNotVisibleException e)      { raiseError(e, executedCommands, driver); }
			catch(InvalidCoordinatesException e)     { raiseError(e, executedCommands, driver); }
			catch(InvalidElementStateException e)    { raiseError(e, executedCommands, driver); }
			catch(JsonException e)                   { raiseError(e, executedCommands, driver); }
			catch(MoveTargetOutOfBoundsException e)  { raiseError(e, executedCommands, driver); }
			catch(StaleElementReferenceException e)  { raiseError(e, executedCommands, driver); }
			catch(TimeoutException e)                { raiseError(e, executedCommands, driver); }
			catch(TooManyElementsFoundException e)   { raiseError(e, executedCommands, driver); }
			catch(UnexpectedTagNameException e)      { raiseError(e, executedCommands, driver); }
			catch(XPathLookupException e)            { raiseError(e, executedCommands, driver); }
			
			// Driver initialization
			catch(CapabilitiesNotFoundException e)   { raiseFailure(e); }
			catch(MalformedURLException e)           { raiseFailure(e); }
			catch(SeleniumNodeNameException e)       { raiseFailure(e); }
			
			// Command execution
			catch(InterruptedException e)            { raiseFailure(e, executedCommands); }
			catch(WebDriverException e)              { raiseFailure(e, executedCommands); }
			catch(RuntimeException e)                { raiseFailure(e, executedCommands); }
			
			finally {
				
				// Consider the driver as closed if TIMEOUT
				if(this.getTestCase().hasFailed() && (
					   this.getTestCase().getFailure().getMessage().matches(".*Session \\[[0-9]+\\] was terminated due to TIMEOUT(?s).*")
					|| this.getTestCase().getFailure().getMessage().matches(".*Error communicating with the remote browser. It may have died\\.(?s).*")
				)) {
					driver = null;
				}
				
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
	
	synchronized protected SeleneseWebDriver initWebDriver() throws MalformedURLException, CapabilitiesNotFoundException {
		final Log log = getLog();
		boolean keepRetrying = true;
		while(keepRetrying) {
			try {
				keepRetrying = false;
				return new SeleneseWebDriver(getBaseUrl(), getServerURL(), getCapability().toCapabilities(), new com.github.amercier.selenium.selenese.log.Log() {
					public void warn (String message) { log.warn (TestCaseRunner.this + " " + message); }
					public void info (String message) { log.info (TestCaseRunner.this + " " + message); }
					public void error(String message) { log.error(TestCaseRunner.this + " " + message); }
					public void debug(String message) { log.debug(TestCaseRunner.this + " " + message); }
				}, getWaitTimeout());
			}
			catch(UnreachableBrowserException e) {
				getLog().warn(e);
				keepRetrying = true;
			}
		}
		return null;
	}
	
	/*
	public static String getHostName(InetAddress host) throws UnknownHostException {
		try {
			Class<?> clazz = Class.forName("java.net.InetAddress");
			Constructor<?>[] constructors = clazz.getDeclaredConstructors();
			constructors[0].setAccessible(true);
			InetAddress ina = (InetAddress) constructors[0].newInstance();
			NameService ns;

			Field[] fields = ina.getClass().getDeclaredFields();
			for(Field field : fields) {
				if(field.getName().equals("nameService")) {
					field.setAccessible(true);
					Method[] methods = field.get(null).getClass().getDeclaredMethods();
					for (Method method : methods) {
						if (method.getName().equals("getHostByAddr")) {
							method.setAccessible(true);
							return (String) method.invoke(field.get(null), host.getAddress());
						}
					}
				}
			}
		}
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (InvocationTargetException e) {
			throw (UnknownHostException) e.getCause();
		}
		return null;
	}
	*/
	
	public String getNodeName(SeleneseWebDriver driver) throws SeleniumNodeNameException {
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
			String nodeName;
			if(response.getBoolean("success")) {
				nodeName = response.getString("proxyId").replaceAll("^http:\\/\\/(.*):[0-9]+$", "$1");
			}
			else {
				throw new SeleniumNodeNameException(response.getString("msg"));
			}
			
			/*
			if(nodeName.matches("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$")) {
				try {
					nodeName = getHostName(InetAddress.getByName(nodeName)) + " (" + nodeName + ")";
				}
				catch(UnknownHostException e) {
					e.printStackTrace();
				}
			}
			*/
			
			return nodeName;
		}
		catch(SeleniumNodeNameException e) {
			throw e;
		}
		catch(Exception e) {
			throw new SeleniumNodeNameException(e.getClass().getName() + ": " + e.getMessage());
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
