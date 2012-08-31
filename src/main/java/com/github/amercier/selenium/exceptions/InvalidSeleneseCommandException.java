package com.github.amercier.selenium.exceptions;

import com.github.amercier.selenium.selenese.SeleneseCommand;

@SuppressWarnings("serial")
public class InvalidSeleneseCommandException extends Exception {
	
	public SeleneseCommand command;

	public InvalidSeleneseCommandException(SeleneseCommand command) {
		super("Invalid Selenese command \"" + command + "\"");
		this.command = command;
	}

	public InvalidSeleneseCommandException(SeleneseCommand command, String argument) {
		super("Invalid Selenese command \"" + command + "\" (argument \"" + argument + "\" is invalid)");
		this.command = command;
	}
}
