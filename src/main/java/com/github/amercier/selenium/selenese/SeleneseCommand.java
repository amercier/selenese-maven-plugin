package com.github.amercier.selenium.selenese;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.SessionId;

public class SeleneseCommand {
	
	/**
	 * Minimum command length
	 */
	private static final int COMMAND_LENGTH_MIN = 1;
	
	protected String name;
	protected List<String> arguments;
	
	public SeleneseCommand(String command) throws InvalidSeleneseCommandNameException {
		this.setName(command);
		this.arguments = new LinkedList<String>();
	}
	
	public String getName() {
		return name;
	}
	
	public SeleneseCommand setName(String name) throws InvalidSeleneseCommandNameException {
		if(name == null || name.length() < COMMAND_LENGTH_MIN) {
			throw new InvalidSeleneseCommandNameException(name);
		}
		this.name = name;
		return this;
	}
	
	public String[] getArguments() {
		return arguments.toArray(new String[0]);
	}
	
	public String getArgument(int index) {
		return this.arguments.get(index);
	}
	
	public SeleneseCommand addArgument(String argument) {
		this.arguments.add(argument);
		return this;
	}
	
	@Override
	public String toString() {
		return getName() + "(" + Arrays.toString(getArguments()).replaceAll("(^\\[|\\]$)", "") + ")";
	}
	
	public Command toCommand(SessionId sessionId) {
		return new Command(sessionId, this.getName(), this.getArgumentsAsMap());
	}
	
	protected Map<String,Object> getArgumentsAsMap() {
		Map<String,Object> parameters = new HashMap<String,Object>();
		String[] arguments = getArguments();
		for(int i = 0 ; i < arguments.length ; i++) {
			parameters.put("" + i, (Object)arguments[i]);
		}
		return parameters;
	}
}
