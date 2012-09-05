package com.github.amercier.selenium.selenese;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String-match Patterns.
 * See {@link http://release.seleniumhq.org/selenium-core/1.0.1/reference.html#patterns}
 */
public enum StringMatchPattern {
	
	PATTERN_REGEXP (Pattern.compile("^regexp:(.*)$")),
	PATTERN_REGEXPI(Pattern.compile("^regexp:(.*)$/i")),
	PATTERN_GLOB   (Pattern.compile("^glob:(.*)$")),
	PATTERN_EXACT  (Pattern.compile("^(exact:)?(.*)$"));
	
	private final Pattern pattern;
	
	private StringMatchPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public Pattern find(CharSequence input) {
		Matcher matcher = pattern.matcher(input);
		if(matcher.find()) {
			switch(this) {
				case PATTERN_REGEXP : return Pattern.compile(matcher.group(1));
				case PATTERN_REGEXPI: return Pattern.compile(matcher.group(1));
				case PATTERN_GLOB   : // 1. Quote everything, including ? and *
				                      // 2. Replace quoted \? with .? and \* with .*
				                      return Pattern.compile("^" + Pattern.quote(matcher.group(1)).replaceAll("\\\\([\\?|\\*])", ".$1") + "$");
				case PATTERN_EXACT  : return Pattern.compile("^" + Pattern.quote(matcher.group(2)) + "$");
			}
		}
		return null; // if not found
	}
}
