package com.github.amercier.selenium.selenese.log;

public class DefaultLog implements Log {
	
	public void info(String message) {
		System.out.println(message);
	}
	
	public void debug(String message) {
		System.out.println(message);
	}
	
	public void warn(String message) {
		System.err.println(message);
	}
	
	public void error(String message) {
		System.err.println(message);
	}
}
