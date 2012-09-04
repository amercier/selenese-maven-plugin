package com.github.amercier.selenium.selenese;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Option locators.
 * See {@link http://release.seleniumhq.org/selenium-core/1.0.1/reference.html#select}
 */
public enum OptionLocator {
	
	REGEXP(Pattern.compile("^label=regexp:(.*)$")),
	LABEL (Pattern.compile("^label=(.*)$")),
	VALUE (Pattern.compile("^value=(.*)$")),
	ID    (Pattern.compile("^id=(.*)$"));
	
	private final Pattern pattern;
	private final int     group;
	
	private OptionLocator(Pattern pattern, int group) {
		this.pattern = pattern;
		this.group = group;
	}
	
	private OptionLocator(Pattern pattern) {
		this(pattern, 1);
	}
	
	
	public String find(CharSequence input) {
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(group) : null;
	}
}
