package com.github.amercier.selenium.selenese;


/**
 * A test case is an object having a name and a lavel, and containing a list of
 * commands
 */
public class SeleneseTestCase {

	public String label;
	public String name;
	public SeleneseCommand commands[];
	
	public String getLabel() {
		return label;
	}
	
	public String getName() {
		return name;
	}
	
	public SeleneseCommand[] getCommands() {
		return commands;
	}
}
