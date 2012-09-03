package com.github.amercier.selenium.selenese.assertions;

import java.util.regex.Pattern;

public class Assert {

	public static void assertPatternMatches(Pattern pattern, String target) throws AssertionFailedException {
		if(!pattern.matcher(target).find()) {
			throw new AssertionFailedException("\"" + target + "\" does not match the regular expression " + pattern.toString());
		}
	}
}