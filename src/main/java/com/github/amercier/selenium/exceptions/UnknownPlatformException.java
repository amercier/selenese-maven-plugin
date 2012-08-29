package com.github.amercier.selenium.exceptions;

import org.openqa.selenium.Platform;

@SuppressWarnings("serial")
public class UnknownPlatformException extends Exception {
	
	public UnknownPlatformException(String platform) {
		super("Unknown platform \"" + platform + "\", must be one of the following: " + Platform.values());
	}
	
}
