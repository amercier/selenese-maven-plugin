package com.github.amercier.selenium.selenese;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandArgumentException;
import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;

public class SeleneseCommand {
	
	/**
	 * Variable pattern: ${variable}
	 */
	public static final Pattern PATTERN_VARIABLE = Pattern.compile("\\$\\{([^\\}]+)\\}");
	
	protected Action action;
	protected List<String> arguments;
	protected Map<String, String> variables;
	
	public SeleneseCommand(Action action, String[] arguments) throws InvalidSeleneseCommandException {
		this.setAction(action);
		
		this.arguments = new LinkedList<String>();
		for(int i = 0 ; i < arguments.length && i < action.getArgumentsCount() ; i++) {
			this.arguments.add(i >= arguments.length ? "" : arguments[i]);
		}
		
		this.variables = null;
		
		if(arguments.length > action.getArgumentsCount()) {
			throw new InvalidSeleneseCommandException(this, "expecting " + action.getArgumentsCount() + " arguments, " + arguments.length + " given");
		}
	}
	
	public Action getAction() {
		return action;
	}
	
	public SeleneseCommand setAction(Action action) {
		this.action = action;
		return this;
	}
	
	protected String[] getRawArguments() {
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
		return getAction() + "(" + Arrays.toString(getRawArguments()).replaceAll("(^\\[|\\]$)", "") + ")";
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
