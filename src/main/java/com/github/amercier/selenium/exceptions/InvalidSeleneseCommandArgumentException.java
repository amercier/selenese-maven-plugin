package com.github.amercier.selenium.exceptions;

@SuppressWarnings("serial")
public class InvalidSeleneseCommandArgumentException extends Exception {

	public String argument;
	
	public InvalidSeleneseCommandArgumentException(String argument) {
		super("Invalid Selenese command argument \"" + argument + "\"");
		this.argument = argument;
	}
	
}
