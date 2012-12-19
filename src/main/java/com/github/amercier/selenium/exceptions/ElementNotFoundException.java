package com.github.amercier.selenium.exceptions;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriverException;

@SuppressWarnings("serial")
public class ElementNotFoundException extends WebDriverException {
	
	public ElementNotFoundException(By by) {
		super("Can't find element " + by);
	}

	public ElementNotFoundException(By by, SearchContext context) {
		super("Can't find element " + by + " in context " + context);
	}
	
}
