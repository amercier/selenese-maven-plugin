package com.github.amercier.selenium.selenese;

import org.openqa.selenium.WebDriver;

import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;

public class SeleneseCommandInterpreter {

	protected WebDriver driver;
	
	public SeleneseCommandInterpreter(WebDriver driver) {
		this.setDriver(driver);
	}
	
	protected void setDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	public void execute(SeleneseCommand command) throws UnknownSeleneseCommandException {
		if(false) {
			// FIXME implement commands
		}
		else { throw new UnknownSeleneseCommandException(command); }
	}
}
