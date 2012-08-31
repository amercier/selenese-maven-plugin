package com.github.amercier.selenium.exceptions;

@SuppressWarnings("serial")
public class InvalidSeleneseCommandException extends Exception {
	
	public String command;

	public InvalidSeleneseCommandException(String command) {
		super("Invalid Selenese command \"" + command + "\"");
		this.command = command;
	}
}
