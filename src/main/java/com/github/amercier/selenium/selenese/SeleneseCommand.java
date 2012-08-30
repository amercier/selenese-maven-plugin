package com.github.amercier.selenium.selenese;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SeleneseCommand {
	
	protected String command;
	protected List<String> arguments;
	
	public SeleneseCommand(String command) {
		this.setCommand(command);
		this.arguments = new LinkedList<String>();
	}
	
	public String getCommand() {
		return command;
	}
	
	public SeleneseCommand setCommand(String command) {
		this.command = command;
		return this;
	}
	
	public String[] getArguments() {
		return arguments.toArray(new String[0]);
	}
	
	public SeleneseCommand addArgument(String argument) {
		this.arguments.add(argument);
		return this;
	}
	
	@Override
	public String toString() {
		return command + "(" + Arrays.toString(getArguments()).replaceAll("(^\\[|\\]$)", "") + ")";
	}
}
