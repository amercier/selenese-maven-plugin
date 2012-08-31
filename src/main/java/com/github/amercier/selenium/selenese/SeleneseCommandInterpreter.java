package com.github.amercier.selenium.selenese;

import java.net.URL;

import org.openqa.selenium.WebDriver;

import com.github.amercier.selenium.exceptions.UnknownSeleneseCommandException;

public class SeleneseCommandInterpreter {

	protected WebDriver driver;
	
	protected URL baseUrl;
	
	public SeleneseCommandInterpreter(WebDriver driver, URL baseUrl) {
		this.setDriver(driver);
		this.setBaseUrl(baseUrl);
	}
	
	protected WebDriver getDriver() {
		return driver;
	}
	
	protected void setDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	public URL getBaseUrl() {
		return baseUrl;
	}
	
	protected void setBaseUrl(URL baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public void execute(SeleneseCommand command) throws UnknownSeleneseCommandException {
		String cmd = command.getCommand();
		
		if("open".equals(cmd)) { driver.get(getBaseUrl().toString().replaceAll("/$","") + command.getArguments()[0]); }
		
		// Fail if command is unknown
		else { throw new UnknownSeleneseCommandException(command); }
	}
}
