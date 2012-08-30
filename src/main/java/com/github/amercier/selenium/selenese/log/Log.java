package com.github.amercier.selenium.selenese.log;

public interface Log {

	public void debug(String message);
	
	public void info(String message);
	
	public void warn(String message);
	
	public void error(String message);
}
