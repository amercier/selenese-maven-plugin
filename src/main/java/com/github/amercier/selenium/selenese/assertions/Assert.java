package com.github.amercier.selenium.selenese.assertions;

import java.util.regex.Pattern;

public class Assert {

	public static void assertPatternMatches(Pattern pattern, String target) throws AssertionFailedException {
		if(!pattern.matcher(target).find()) {
			throw new AssertionFailedException("\"" + target + "\" does not match the regular expression " + pattern.toString());
		}
	}
	
	public static void assertNotNull(Object value, String message) throws AssertionFailedException {
		if(value == null) {
			throw new AssertionFailedException(message);
		}
	}
	
	public static void assertNull(Object value, String message) throws AssertionFailedException {
		if(value != null) {
			throw new AssertionFailedException(message);
		}
	}

	public static void assertEqual(int actual, int expected, String message) throws AssertionFailedException {
		if(actual != expected) {
			throw new AssertionFailedException(message);
		}
	}

	public static void assertEqual(String actual, String expected, String message) throws AssertionFailedException {
		if(!actual.equals(expected)) {
			throw new AssertionFailedException(message);
		}
	}

	public static void assertNotEqual(String actual, String unexpected, String message) throws AssertionFailedException {
		if(actual.equals(unexpected)) {
			throw new AssertionFailedException(message);
		}
	}

	public static void assertNotEqual(int actual, int unexpected, String message) throws AssertionFailedException {
		if(actual == unexpected) {
			throw new AssertionFailedException(message);
		}
	}
}