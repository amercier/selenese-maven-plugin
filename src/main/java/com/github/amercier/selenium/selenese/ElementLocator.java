package com.github.amercier.selenium.selenese;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element locators.
 * See {@link http://release.seleniumhq.org/selenium-core/1.0.1/reference.html#locators}
 */
public enum ElementLocator {
	
	NAME (Pattern.compile("^name=(.*)$")),
	LINK (Pattern.compile("^link=(.*)$")),
	CSS  (Pattern.compile("^css=(.*)$")),
	XPATH(Pattern.compile("^(xpath=)?(//.*)$")),
	ID   (Pattern.compile("^(id(entifier)?=)?(.*)$")); // ID must be the last one as it matches everything
	
	private final Pattern pattern;
	
	private ElementLocator(Pattern pattern) {
		this.pattern = pattern;
	}
	
	private int getGroup() {
		switch(this) {
			case ID   : return 3;
			case XPATH: return 2;
			default   : return 1;
		}
	}
	
	public String find(CharSequence input) {
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(getGroup()) : null;
	}
}
