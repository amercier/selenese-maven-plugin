package com.github.amercier.selenium.exceptions;

import com.github.amercier.selenium.selenese.SeleneseCommand;

@SuppressWarnings("serial")
public class UnknownSeleneseCommandException extends Exception {
	
	protected SeleneseCommand command;
	
	public UnknownSeleneseCommandException(SeleneseCommand command) {
		super("Unknown Selenese command \"" + command.getName() + "\"");
		this.command = command;
	}

}
