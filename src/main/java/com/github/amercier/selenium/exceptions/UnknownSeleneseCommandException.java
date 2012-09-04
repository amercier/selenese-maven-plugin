package com.github.amercier.selenium.exceptions;


@SuppressWarnings("serial")
public class UnknownSeleneseCommandException extends Exception {
	
	protected String actionName;
	
	public UnknownSeleneseCommandException(String actionName) {
		super("Unknown Selenese command \"" + actionName + "\"");
		this.actionName = actionName;
	}

}
