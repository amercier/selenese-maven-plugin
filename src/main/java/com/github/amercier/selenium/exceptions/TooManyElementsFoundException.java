package com.github.amercier.selenium.exceptions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

@SuppressWarnings("serial")
public class TooManyElementsFoundException extends WebDriverException {

	public TooManyElementsFoundException(By locator, int found) {
		super("Found " + found + " element matching " + locator + " where 1 was expected");
	}

}
