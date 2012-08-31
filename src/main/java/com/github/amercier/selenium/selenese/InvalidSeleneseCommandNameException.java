package com.github.amercier.selenium.selenese;

@SuppressWarnings("serial")
public class InvalidSeleneseCommandNameException extends Exception {
	
	public String name;
	
	public InvalidSeleneseCommandNameException(String name) {
		super("Invalid Selenese command name \"" + name + "\"");
		this.name = name;
	}

}
