package com.github.amercier.selenium.selenese;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;

public class SeleneseCommand {
	
	/**
	 * Minimum command length
	 */
	private static final int COMMAND_LENGTH_MIN = 1;
	
	protected String command;
	protected List<String> arguments;
	
	public SeleneseCommand(String command) throws InvalidSeleneseCommandException {
		this.setCommand(command);
		this.arguments = new LinkedList<String>();
	}
	
	public String getCommand() {
		return command;
	}
	
	public SeleneseCommand setCommand(String command) throws InvalidSeleneseCommandException {
		if(command == null || command.length() < COMMAND_LENGTH_MIN) {
			throw new InvalidSeleneseCommandException(command);
		}
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
