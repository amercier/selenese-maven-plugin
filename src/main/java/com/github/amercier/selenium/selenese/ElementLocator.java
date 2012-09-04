package com.github.amercier.selenium.selenese;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandArgumentException;

/**
 * Element locators.
 * See {@link http://release.seleniumhq.org/selenium-core/1.0.1/reference.html#locators}
 */
public enum ElementLocator {
	
	NAME (Pattern.compile("^name=(.*)$")),
	LINK (Pattern.compile("^link=(.*)$")),
	CSS  (Pattern.compile("^css=(.*)$")),
	XPATH(Pattern.compile("^(xpath=)?(//.*)$"), 2),
	ID   (Pattern.compile("^(id(entifier)?=)?(.*)$"), 3); // ID must be the last one as it matches everything
	
	private final Pattern pattern;
	private final int     group;
	
	private ElementLocator(Pattern pattern, int group) {
		this.pattern = pattern;
		this.group = group;
	}
	
	private ElementLocator(Pattern pattern) {
		this(pattern, 1);
	}
	
	public String find(CharSequence input) {
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(group) : null;
	}
	
	public static By parse(String elementLocator) throws InvalidSeleneseCommandArgumentException {
		String matched;
		for(ElementLocator locator : ElementLocator.values()) {
			if((matched = locator.find(elementLocator)) != null) {
				switch(locator) {
					case ID   : return By.id(matched);
					case NAME : return By.name(matched);
					case XPATH: return By.xpath(matched);
					case LINK : return By.linkText(matched);
					case CSS  : return By.cssSelector(matched);
				}
			}
		}
		throw new InvalidSeleneseCommandArgumentException(elementLocator);
	}
}
