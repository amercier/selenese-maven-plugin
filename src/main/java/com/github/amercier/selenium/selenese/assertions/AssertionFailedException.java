package com.github.amercier.selenium.selenese.assertions;


@SuppressWarnings("serial")
public class AssertionFailedException extends Exception {
	
	/*
	protected SeleneseCommand command;
	
	public void setCommand(SeleneseCommand command) {
		this.command = command;
	}
	
	public SeleneseCommand getCommand() {
		return command;
	}
	
	public AssertionFailedException(SeleneseCommand command, String message) {
		super(message);
		this.setCommand(command);
	}
	*/
	
	public AssertionFailedException(String message) {
		super(message);
	}
}
