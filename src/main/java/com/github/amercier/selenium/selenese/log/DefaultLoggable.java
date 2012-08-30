package com.github.amercier.selenium.selenese.log;

public abstract class DefaultLoggable {
	
	protected Log log;
	
	public DefaultLoggable(Log log) {
		this.setLog(log);
	}
	
	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}
}
