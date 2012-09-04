package com.github.amercier.selenium.selenese;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandArgumentException;

public class SeleneseCommand {
	
	/**
	 * Minimum command length
	 */
	private static final int COMMAND_LENGTH_MIN = 1;
	
	/**
	 * 
	 */
	public static final Pattern PATTERN_VARIABLE = Pattern.compile("\\$\\{([^\\}]+)\\}");
	
	protected String name;
	protected List<String> arguments;
	protected Map<String, String> variables;
	
	public SeleneseCommand(String command) throws InvalidSeleneseCommandNameException {
		this.setName(command);
		this.arguments = new LinkedList<String>();
		this.variables = null;
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
	
	protected String[] getArguments() {
		return arguments.toArray(new String[0]);
	}
	
	protected String getRawArgument(int index) {
		return this.arguments.get(index);
	}
	
	public String getArgument(int index) throws InvalidSeleneseCommandArgumentException, IllegalAccessException {
		if(variables == null) {
			throw new IllegalAccessException("Can not parse arguments while variables are not set.");
		}
		String argument = getRawArgument(index);
		Matcher matcher = PATTERN_VARIABLE.matcher(argument);
		while(matcher.find()) {
			
			// Extract variable
			String variableName = matcher.group(1);
			
			// Ensure variable is set
			if(!variables.containsKey(variableName)) {
				throw new InvalidSeleneseCommandArgumentException(argument + " (variable " + variableName + " is not set)");
			}
			
			// Replace variable in argument
			argument = argument.replace("${" + variableName + "}", variables.get(variableName));
			
			// Update matcher
			matcher = PATTERN_VARIABLE.matcher(argument);
		}
		
		return argument;
	}
	
	public SeleneseCommand addArgument(String argument) {
		this.arguments.add(argument);
		return this;
	}
	
	public void setVariables(Map<String,String> variables) {
		this.variables = variables;
	}
	
	@Override
	public String toString() {
		return getName() + "(" + Arrays.toString(getArguments()).replaceAll("(^\\[|\\]$)", "") + ")";
	}
	
	/*
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
	*/
}
