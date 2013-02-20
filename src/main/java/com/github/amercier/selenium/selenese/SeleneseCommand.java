package com.github.amercier.selenium.selenese;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandException;

public class SeleneseCommand {
	
	/**
	 * Variable pattern: ${variable}
	 */
	//public static final Pattern PATTERN_VARIABLE = Pattern.compile("\\$\\{([^\\}]+)\\}");
	
	protected Action action;
	protected List<String> arguments;
	protected Map<String, String> variables;
	
	public SeleneseCommand(Action action, String[] arguments) throws InvalidSeleneseCommandException {
		this.setAction(action);
		
		/* To deal with optional arguments, we add all the given arguments, and
		 * then we add empty arguments "" until action.getArgumentsCount() is reached
		 */
		this.arguments = new LinkedList<String>();
		for(int i = 0 ; i < arguments.length || i < action.getArgumentsCount() ; i++) {
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
		return arguments.get(index);
	}
	
	public String getArgument(int index) throws IllegalAccessException {
		if(variables == null) {
			throw new IllegalAccessException("Can not parse arguments while variables are not set.");
		}
		String argument = getRawArgument(index);
		
		for(Entry<String,String> variable : variables.entrySet()) {
			argument = argument.replace("${" + variable.getKey() + "}", variable.getValue());
		}
		
		return argument;
	}
	
	public String[] getArguments() {
		String[] arguments = new String[this.arguments.size()];
		for(int i = 0 ; i < arguments.length ; i++) {
			try {
				arguments[i] = getArgument(i);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return arguments;
	}
	
	public void setVariables(Map<String,String> variables) {
		this.variables = variables;
	}
	
	@Override
	public String toString() {
		return getAction() + "(" + Arrays.toString(variables == null ? getRawArguments() : getArguments()).replaceAll("(^\\[|\\]$)", "") + ")";
	}
}
