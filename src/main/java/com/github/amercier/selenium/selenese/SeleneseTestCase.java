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
	protected Throwable error;
	protected Throwable failure;
	
	public SeleneseTestCase(String name) {
		this.setName(name);
		this.commands = new LinkedList<SeleneseCommand>();
		this.error = null;
		this.failure = null;
	}
	
	public String getName() {
		return name;
	}
	
	public SeleneseTestCase setName(String name) {
		this.name = name;
		return this;
	}
	
	public SeleneseTestCase addCommand(SeleneseCommand command) {
		this.commands.add(command);
		return this;
	}
	
	public SeleneseCommand[] getCommands() {
		return commands.toArray(new SeleneseCommand[0]);
	}
	
	public boolean hasErrored() {
		return error != null;
	}

	public void setError(Throwable error) {
		if(this.error == null) {
			this.error = error;
		}
	}
	
	public Throwable getError() {
		return error;
	}
	
	public boolean hasFailed() {
		return failure != null;
	}
	
	public void setFailure(Throwable failure) {
		if(this.failure == null) {
			this.failure = failure;
		}
	}
	
	public Throwable getFailure() {
		return failure;
	}
	
	public boolean hasSucceeded() {
		return error == null && failure == null;
	}
	
	public String getStatus() {
		if(this.hasFailed()) {
			return "FAILURE";
		}
		else if(this.hasErrored()) {
			return "ERROR";
		}
		else {
			return "SUCCESS";
		}
	}
	
	public SeleneseTestCase cloneWithoutState() {
		SeleneseTestCase clone = new SeleneseTestCase(getName());
		for(SeleneseCommand command : getCommands()) {
			clone.addCommand(command);
		}
		return clone;
	}
}
