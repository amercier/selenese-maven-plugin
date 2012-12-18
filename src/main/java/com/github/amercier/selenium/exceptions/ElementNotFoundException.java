package com.github.amercier.selenium.exceptions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

@SuppressWarnings("serial")
public class ElementNotFoundException extends WebDriverException {
	
	public ElementNotFoundException(By locator) {
		super("Can't find element " + locator);
	}
	
}
