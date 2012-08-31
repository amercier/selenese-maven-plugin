package com.github.amercier.selenium.selenese;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandArgumentException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;
import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;
import com.google.common.base.Predicate;

public class SeleneseWebDriver extends RemoteWebDriver {
	
	/**
	 * Element locators.
	 * See {@link http://release.seleniumhq.org/selenium-core/1.0.1/reference.html#locators}
	 */
	public static Pattern LOCATOR_IDENTIFIER = Pattern.compile("^id=(.*)$");
	public static Pattern LOCATOR_ID         = Pattern.compile("^id=(.*)$");
	public static Pattern LOCATOR_NAME       = Pattern.compile("^id=(.*)$");
	public static Pattern LOCATOR_DOM        = Pattern.compile("^id=(.*)$");
	public static Pattern LOCATOR_XPATH      = Pattern.compile("^id=(.*)$");
	public static Pattern LOCATOR_LINK       = Pattern.compile("^id=(.*)$");
	public static Pattern LOCATOR_CSS        = Pattern.compile("^id=(.*)$");
	
	/**
	 * String-match Patterns.
	 * See {@link http://release.seleniumhq.org/selenium-core/1.0.1/reference.html#patterns}
	 */
	public static Pattern PATTERN_REGEXP  = Pattern.compile("^regexp:(.*)$");
	public static Pattern PATTERN_REGEXPI = Pattern.compile("^regexp:(.*)$/i");
	public static Pattern PATTERN_EXACT   = Pattern.compile("^exact:(.*)$");
	public static Pattern PATTERN_GLOB    = Pattern.compile("^glob:(.*)$");
	
	public static long PAUSE_CHECK_INTERVAL = (long)1.0;
	
	protected URL baseURL;
	
	public SeleneseWebDriver(URL baseURL, URL remoteAddress, DesiredCapabilities desiredCapabilities) {
		super(remoteAddress, desiredCapabilities);
		setBaseURL(baseURL);
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
		Matcher matcher;
		if((matcher = LOCATOR_ID.matcher(seleneseSelector)).matches()) {
			return findElementById(matcher.group(1));
		}
		else {
			throw new InvalidSeleneseCommandArgumentException(seleneseSelector);
		}
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
	
	protected Pattern parsePattern(String selenesePattern) throws InvalidSeleneseCommandArgumentException {
		if(PATTERN_REGEXP.matcher(selenesePattern).matches()) {
			return Pattern.compile(selenesePattern);
		}
		else if(PATTERN_EXACT.matcher(selenesePattern).matches()) {
			return Pattern.compile("^" + Pattern.quote(selenesePattern) + "$");
		}
		else if(PATTERN_GLOB.matcher(selenesePattern).matches()) {
			// 1. Quote everything, including ? and *
			// 2. Replace quoted \? with .? and \* with .*
			return Pattern.compile("^" + Pattern.quote(selenesePattern).replaceAll("\\\\([\\?|\\*])", ".$1") + "$");
		}
		else {
			throw new InvalidSeleneseCommandArgumentException(selenesePattern);
		}
	}
	
	public void execute(SeleneseCommand command) throws InvalidSeleneseCommandException, UnknownSeleneseCommandException, InterruptedException {
		
		String cmd = command.getName();
		
		System.out.println("Executing " + command);
		
		try {
			if("open".equals(cmd)) {
				get(getAbsoluteURL(command.getArgument(0)));
				/*  TODO Remove waiting for dom to be ready
				new WebDriverWait(this, (long)30.0).until(new Predicate<WebDriver>() {
					public boolean apply(WebDriver input) {
						System.out.println("document.readyState = " + SeleneseWebDriver.this.executeScript("return document.readyState", new Object[0]));
						return SeleneseWebDriver.this.executeScript("return document.readyState", new Object[0]).equals("complete");
					}
				});
				*/
			}
			else if("type"          .equals(cmd)) { findElement(command.getArgument(0)).sendKeys(command.getArgument(1)); }
			else if("click"         .equals(cmd)) { findElement(command.getArgument(0)).click(); }
			else if("pause"         .equals(cmd)) { pause(Long.parseLong(command.getArgument(0))); }
			else if("assertLocation".equals(cmd)) { Assert.assertTrue(parsePattern(command.getArgument(0)).matcher(getCurrentUrl()).matches()); }
			else {
				throw new UnknownSeleneseCommandException(command);
			}
		}
		catch(InvalidSeleneseCommandArgumentException e) {
			throw new InvalidSeleneseCommandException(command, e.argument);
		}
	}
}
