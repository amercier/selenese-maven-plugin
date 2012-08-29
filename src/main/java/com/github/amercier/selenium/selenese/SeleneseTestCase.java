package com.github.amercier.selenium.selenese;

import java.util.LinkedList;
import java.util.List;


/**
 * A test case is an object having a name and a lavel, and containing a list of
 * commands
 */
public class SeleneseTestCase {

	protected String name;
	protected List<SeleneseCommand> commands;
	
	public SeleneseTestCase(String name) {
		this.setName(name);
		this.commands = new LinkedList<SeleneseCommand>();
	}
	
	public String getName() {
		return name;
	}
	
	public SeleneseTestCase addCommand(SeleneseCommand command) {
		this.commands.add(command);
		return this;
	}
	
	public SeleneseCommand[] getCommands() {
		return commands.toArray(new SeleneseCommand[0]);
	}

	public SeleneseTestCase setName(String name) {
		this.name = name;
		return this;
	}
}
