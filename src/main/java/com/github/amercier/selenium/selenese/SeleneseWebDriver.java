package com.github.amercier.selenium.selenese;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.amercier.selenium.exceptions.ElementNotFoundException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandArgumentException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.TooManyElementsFoundException;
import com.github.amercier.selenium.selenese.assertions.Assert;
import com.github.amercier.selenium.selenese.assertions.AssertionFailedException;
import com.github.amercier.selenium.selenese.log.Log;
import com.github.amercier.selenium.selenese.log.Loggable;
import com.google.common.base.Predicate;

public class SeleneseWebDriver extends RemoteWebDriver implements Loggable {
	
	/**
	 * Storage to be used with storeEval and loadEval
	 */
	protected Map<String,String> storage;
	
	protected URL baseURL;
	
	protected Log log;
	
	protected long waitTimeout;
	
	public SeleneseWebDriver() {
		super();
	}
	
	public SeleneseWebDriver(URL baseURL, URL remoteAddress, DesiredCapabilities desiredCapabilities, Log log, long waitTimeout) {
		super(remoteAddress, desiredCapabilities);
		setBaseURL(baseURL);
		this.storage = new HashMap<String,String>();
		setLog(log);
		setWaitTimeout(waitTimeout);
	}

	public URL getBaseURL() {
		return baseURL;
	}
	
	protected void setBaseURL(URL remoteAddress) {
		this.baseURL = remoteAddress;
	}
	
	public Log getLog() {
		return log;
	}
	
	public void setLog(Log log) {
		this.log = log;
	}
	
	public long getWaitTimeout() {
		return waitTimeout;
	}
	
	public void setWaitTimeout(long waitTimeout) {
		this.waitTimeout = waitTimeout;
	}
	
	protected String getAbsoluteURL(String relativeURL) {
		return getBaseURL().toString().replaceAll("/$","") + "/" + relativeURL.replaceAll("^/","");
	}
	
	protected Pattern parsePattern(String selenesePattern) throws InvalidSeleneseCommandArgumentException {
		Pattern result;
		for(StringMatchPattern stringMatcher : StringMatchPattern.values()) {
			if((result = stringMatcher.find(selenesePattern)) != null) {
				return result;
			}
		}
		throw new InvalidSeleneseCommandArgumentException(selenesePattern);
	}
	
	protected long getTime() {
		Object time = executeScript("return new Date().getTime()", new Object[0]);
		if(time instanceof Long) {
			return ((Long)time).longValue();
		}
		else if(time instanceof Float) {
			return ((Float)time).longValue();
		}
		else if(time instanceof Double) {
			return ((Double)time).longValue();
		}
		else {
			throw new RuntimeException("Can't convert " + time.getClass().getName() + " time " + time + " to long.");
		}
	}
	
	protected void pause(final long milliseconds) {
		final long startTime = getTime();
		new WebDriverWait(this, (long)(milliseconds + 1.0)).until(new Predicate<WebDriver>() {
			public boolean apply(WebDriver input) {
				SeleneseWebDriver driver = (SeleneseWebDriver)input;
				long time = driver.getTime();
				return time >= startTime + milliseconds;
			}
		});
	}
	
	protected WebElement getElement(SearchContext context, By by) throws ElementNotFoundException, TooManyElementsFoundException {
		List<WebElement> elements = by.findElements(context);
		if(elements.size() == 0) {
			if(context == this) {
				throw new ElementNotFoundException(by);
			}
			else {
				throw new ElementNotFoundException(by, context);
			}
		}
		else if(elements.size() > 1) {
			if(context == this) {
				throw new TooManyElementsFoundException(by, elements.size());
			}
			else {
				throw new TooManyElementsFoundException(by, context, elements.size());
			}
		}
		else {
			return elements.get(0);
		}
	}
	
	protected WebElement getElement(By by) throws ElementNotFoundException, TooManyElementsFoundException {
		return getElement(this, by);
	}
	
	protected List<WebElement> getElements(SearchContext context, By by) throws ElementNotFoundException {
		List<WebElement> elements = by.findElements(context);
		if(elements.size() == 0) {
			if(context == this) {
				throw new ElementNotFoundException(by);
			}
			else {
				throw new ElementNotFoundException(by, context);
			}
		}
		
		if(elements.size() > 1) {
			getLog().warn("Warning: found " + elements.size() + " elements matching " + by + (context == this ? "" : " in context " + context));
		}
		return elements;
	}
	
	protected List<WebElement> getElements(By by) throws ElementNotFoundException {
		return getElements(this, by);
	}
	
	synchronized public void execute(final SeleneseCommand command) throws InvalidSeleneseCommandException, InterruptedException, WebDriverException, AssertionFailedException, ElementNotFoundException, TooManyElementsFoundException {
		
		command.setVariables(storage);
		
		try {
			String compiledCommand = command.toCompiledString();
			if(!command.toString().equals(compiledCommand)) {
				getLog().debug("Executing " + compiledCommand);
			}
		}
		catch (IllegalAccessException e1) {
			e1.printStackTrace(); // Will never happen as command.setVariables(storage) is executed just before
		}
		
		try {
			switch(command.getAction()) {
				       case assertElementPresent    : Assert.assertNotEqual(0, this.findElements(ElementLocator.parse(command.getArgument(0))).size(), "Can not find element \"" + command.getArgument(0) + "\"");
				break; case assertElementNotPresent : Assert.assertEqual(0, this.findElements(ElementLocator.parse(command.getArgument(0))).size(), "Element \"" + command.getArgument(0) + "\" is present");
				break; case assertEval              : { String result = "" + executeScript("return (" + command.getArgument(0) + ")", new Object[0]); Assert.assertEqual(result, command.getArgument(1), "Script \"" + command.getArgument(0) + "\" returned \"" + result + "\""); }
				break; case assertLocation          : Assert.assertPatternMatches(parsePattern(command.getArgument(0)), getCurrentUrl());
				break; case assertNotVisible        : Assert.assertFalse(getElement(ElementLocator.parse(command.getArgument(0))).isDisplayed(), "Element \"" + command.getArgument(0) + "\" is visible");
				break; case assertText              : Assert.assertPatternMatches(parsePattern(command.getArgument(1)), getElement(ElementLocator.parse(command.getArgument(0))).getText());
				break; case assertVisible           : Assert.assertTrue(getElement(ElementLocator.parse(command.getArgument(0))).isDisplayed(), "Element \"" + command.getArgument(0) + "\" is visible");
				break; case click                   : for(WebElement e : getElements(ElementLocator.parse(command.getArgument(0)))) if(e.isDisplayed()) e.click();
				break; case check                   : for(WebElement e : getElements(ElementLocator.parse(command.getArgument(0)))) if(e.isDisplayed() && e.getAttribute("checked") == null) e.click();
				break; case dragAndDropToObject     : (new Actions(this)).dragAndDrop( getElement(ElementLocator.parse(command.getArgument(0))), getElement(ElementLocator.parse(command.getArgument(1))) ).perform();
				break; case getEval                 : executeScript(command.getArgument(0), new Object[0]);
				break; case echo                    : System.out.println(executeScript("return ('" + command.getArgument(0) + "')", new Object[0]));
				break; case open                    : get(getAbsoluteURL(command.getArgument(0)));
				break; case pause                   : pause(Long.parseLong(command.getArgument(0)));
				break; case select                  : { for(WebElement e : getElements(ElementLocator.parse(command.getArgument(0)))) if(e.isDisplayed()) new Select(e).selectByValue( getElement(e,OptionLocator.parse(command.getArgument(1))).getAttribute("value") ); }
				break; case storeEval               : storage.put(command.getArgument(1), "" + executeScript("return (" + command.getArgument(0) + ")", new Object[0]));
				break; case type                    : { for(WebElement e : getElements(ElementLocator.parse(command.getArgument(0)))) if(e.isDisplayed()) if(e.isEnabled()) { e.clear(); e.sendKeys(command.getArgument(1)); } else getLog().warn("Warning: element \"" + command.getArgument(0) + "\" is disabled"); }
				break; case uncheck                 : for(WebElement e : getElements(ElementLocator.parse(command.getArgument(0)))) if(e.isDisplayed() && e.getAttribute("checked") != null) e.click();
				break; case waitForElementPresent   : { final By by = ElementLocator.parse(command.getArgument(0)); new WebDriverWait(this, getWaitTimeout() / 1000).until(new ExpectedCondition<Boolean>(){ public Boolean apply(WebDriver d) { return d.findElements(by).size() != 0; }}); }
				break; case waitForElementNotPresent: { final By by = ElementLocator.parse(command.getArgument(0)); new WebDriverWait(this, getWaitTimeout() / 1000).until(new ExpectedCondition<Boolean>(){ public Boolean apply(WebDriver d) { return d.findElements(by).size() == 0; }}); }
				break; case waitForEval             : { final String script = command.getArgument(0); final String expected = command.getArgument(1); new WebDriverWait(this, getWaitTimeout() / 1000).until(new ExpectedCondition<Boolean>(){ public Boolean apply(WebDriver d) { return ("" + executeScript("return (" + script + ")", new Object[0])).equals(expected); }}); }
				break; case waitForLocation         : { final Pattern pattern = parsePattern(command.getArgument(0)); new WebDriverWait(this, getWaitTimeout() / 1000).until(new ExpectedCondition<Boolean>(){ public Boolean apply(WebDriver d) { return pattern.matcher(getCurrentUrl()).find(); }}); }
				break; case waitForNotEval          : { final String script = command.getArgument(0); final String expected = command.getArgument(1); new WebDriverWait(this, getWaitTimeout() / 1000).until(new ExpectedCondition<Boolean>(){ public Boolean apply(WebDriver d) { return !("" + executeScript("return (" + script + ")", new Object[0])).equals(expected); }}); }
				break; case waitForNotLocation      : { final Pattern pattern = parsePattern(command.getArgument(0)); new WebDriverWait(this, getWaitTimeout() / 1000).until(new ExpectedCondition<Boolean>(){ public Boolean apply(WebDriver d) { return !pattern.matcher(getCurrentUrl()).find(); }}); }
				break; case waitForVisible          : { final By by = ElementLocator.parse(command.getArgument(0)); new WebDriverWait(this, getWaitTimeout() / 1000).until(new ExpectedCondition<Boolean>(){ public Boolean apply(WebDriver d) { for(WebElement e : d.findElements(by)) if(e.isDisplayed()) return true; return false; }}); }
			}
		}
		catch(InvalidSeleneseCommandArgumentException e) {
			throw new InvalidSeleneseCommandException(command, "argument " + e.argument + " is invalid");
		}
		catch (IllegalAccessException e) {
			e.printStackTrace(); // (no storage set) Should not happen as we've set storage just before
		}
	}
}
