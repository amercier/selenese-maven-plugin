package com.github.amercier.selenium.selenese;

import java.net.URL;
import java.util.regex.Matcher;
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
		Matcher matcher;
		if((matcher = PATTERN_REGEXP.matcher(selenesePattern)).find()) {
			return Pattern.compile(matcher.group(1));
		}
		else if((matcher = PATTERN_EXACT.matcher(selenesePattern)).find()) {
			return Pattern.compile("^" + Pattern.quote(matcher.group(1)) + "$");
		}
		else if((matcher = PATTERN_GLOB.matcher(selenesePattern)).find()) {
			// 1. Quote everything, including ? and *
			// 2. Replace quoted \? with .? and \* with .*
			return Pattern.compile("^" + Pattern.quote(matcher.group(1)).replaceAll("\\\\([\\?|\\*])", ".$1") + "$");
		}
		else {
			throw new InvalidSeleneseCommandArgumentException(selenesePattern);
		}
	}
	
	public void execute(SeleneseCommand command) throws InvalidSeleneseCommandException, UnknownSeleneseCommandException, InterruptedException, AssertionFailedException {
		
		String cmd = command.getName();
		
		try {
			if("open".equals(cmd)) {
				get(getAbsoluteURL(command.getArgument(0)));
			}
			else if("type"          .equals(cmd)) { findElement(command.getArgument(0)).sendKeys(command.getArgument(1)); }
			else if("click"         .equals(cmd)) { findElement(command.getArgument(0)).click(); }
			else if("pause"         .equals(cmd)) { pause(Long.parseLong(command.getArgument(0))); }
			else if("assertLocation".equals(cmd)) { Assert.assertPatternMatches(parsePattern(command.getArgument(0)), getCurrentUrl()); }
			else {
				throw new UnknownSeleneseCommandException(command);
			}
		}
		catch(InvalidSeleneseCommandArgumentException e) {
			throw new InvalidSeleneseCommandException(command, e.argument);
		}
	}
}
