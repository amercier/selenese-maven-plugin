package com.github.amercier.selenese_maven_plugin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A Selenium Test Command, in HTML format (Selenese).
 */
public class SeleneseCommand {
	
	/**
	 * Command
	 */
	protected String command;
	
	/**
	 * Ordered list of arguments
	 */
	protected List<String> arguments;
	
	public SeleneseCommand(String command, Collection<String> arguments) {
		
		if(command == null) {
			throw new IllegalArgumentException("Expecting command parameter to be a Selenese Command (String), null given.");
		}
		
		this.command = command;
		this.arguments =
				arguments == null
				? new LinkedList<String>()
				: new LinkedList<String>(arguments);
		
	}
}
