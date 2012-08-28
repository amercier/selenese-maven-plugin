package com.github.amercier.selenese_maven_plugin;

import java.util.LinkedList;
import java.util.List;

/**
 * A Selenium Test Case, in HTML format (Selenese).
 */
public class SeleneseTestCase {
	
	/**
	 * The ordered list of commands
	 */
	protected List<SeleneseCommand> commands;
	
	public SeleneseTestCase(List<SeleneseCommand> commands) {
		commands = new LinkedList<SeleneseCommand>();
		if(commands != null) {
			commands.addAll(commands);
		}
	}

}
