package com.github.amercier.selenese_maven_plugin;

import org.apache.maven.plugin.logging.Log;

/**
 * A test case context. Contains all the tools a test case runner needs.
 */
public class TestCaseRunnerContext {
	
	public TestCaseRunnerContext(Log log) {
		
		if(log == null) {
			throw new IllegalArgumentException("Expecting log parameter to be a Log instance, null given.");
		}
		
	}

	/**
	 * The context's log instance
	 */
	protected Log log = null;
	
	/**
	 * Log getter
	 * @return Returns the context's Log instance
	 */
	public Log getLog() {
		return log;
	}
	
	/**
	 * Log setter. Theoretically, this method should not be called from outside
	 * this class.
	 * @param log The log instance
	 */
	public void setLog(Log log) {
		this.log = log;
	}
}
