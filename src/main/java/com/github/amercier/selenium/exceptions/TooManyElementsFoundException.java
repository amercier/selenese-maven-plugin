package com.github.amercier.selenium.exceptions;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriverException;

@SuppressWarnings("serial")
public class TooManyElementsFoundException extends WebDriverException {

	public TooManyElementsFoundException(By by, int found) {
		super("Found " + found + " element matching " + by + " where 1 was expected");
	}

	public TooManyElementsFoundException(By by, SearchContext context, int found) {
		super("Found " + found + " element matching " + by + " where 1 was expected in context " + context);
	}

}
