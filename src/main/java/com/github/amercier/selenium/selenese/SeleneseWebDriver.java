package com.github.amercier.selenium.selenese;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandArgumentException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;
import com.github.amercier.selenium.selenese.assertions.Assert;
import com.github.amercier.selenium.selenese.assertions.AssertionFailedException;
import com.google.common.base.Predicate;

public class SeleneseWebDriver extends RemoteWebDriver {
	
	/**
	 * Storage to be used with storeEval and loadEval
	 */
	protected Map<String,String> storage;
	
	public static long PAUSE_CHECK_INTERVAL = (long)1.0;
	
	protected URL baseURL;
	
	public SeleneseWebDriver(URL baseURL, URL remoteAddress, DesiredCapabilities desiredCapabilities) {
		super(remoteAddress, desiredCapabilities);
		setBaseURL(baseURL);
		this.storage = new HashMap<String,String>();
	}

	public URL getBaseURL() {
		return baseURL;
	}
	
	protected void setBaseURL(URL remoteAddress) {
		this.baseURL = remoteAddress;
	}
	
	protected String getAbsoluteURL(String relativeURL) {
		return getBaseURL().toString().replaceAll("/$","") + "/" + relativeURL.replaceAll("^/","");
	}
	
	protected WebElement findElement(String seleneseSelector) throws InvalidSeleneseCommandArgumentException {
		String matched;
		for(Locator locator : Locator.values()) {
			if((matched = locator.find(seleneseSelector)) != null) {
				switch(locator) {
					case ID   : return findElementById(matched);
					case NAME : return findElementByName(matched);
					case XPATH: return findElementByXPath(matched);
					case LINK : return findElementByLinkText(matched);
					case CSS  : return findElementByCssSelector(matched);
				}
			}
		}
		throw new InvalidSeleneseCommandArgumentException(seleneseSelector);
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
		return (Long) SeleneseWebDriver.this.executeScript("return new Date().getTime()", new Object[0]);
	}
	
	protected void pause(final long milliseconds) {
		final long startTime = getTime();
		new WebDriverWait(this, (long)(milliseconds + 1.0)).until(new Predicate<WebDriver>() {
			public boolean apply(WebDriver input) {
				SeleneseWebDriver driver = (SeleneseWebDriver)input;
				return driver.getTime() >= startTime + milliseconds;
			}
		});
	}
	
	public void execute(SeleneseCommand command) throws InvalidSeleneseCommandException, UnknownSeleneseCommandException, InterruptedException, AssertionFailedException {
		
		command.setVariables(storage);
		
		try {
			switch(command.getAction()) {
				       case open                : get(getAbsoluteURL(command.getArgument(0)));
				break; case type                : WebElement e = findElement(command.getArgument(0)); e.clear(); e.sendKeys(command.getArgument(1));
				break; case click               : findElement(command.getArgument(0)).click();
				break; case pause               : pause(Long.parseLong(command.getArgument(0)));
				break; case assertLocation      : Assert.assertPatternMatches(parsePattern(command.getArgument(0)), getCurrentUrl());
				break; case assertElementPresent: Assert.assertNotNull(this.findElement(command.getArgument(0)), "Can not find element \"" + command.getArgument(0) + "\"");
				break; case storeEval           : storage.put(command.getArgument(1), "" + executeScript("return (" + command.getArgument(0) + ")", new Object[0]));
			}
		}
		catch(InvalidSeleneseCommandArgumentException e) {
			throw new InvalidSeleneseCommandException(command, e.argument);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace(); // (no storage set) Should not happen as we've set storage just before
		}
	}
}
