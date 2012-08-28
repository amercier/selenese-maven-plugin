package com.github.amercier.selenium.maven;

/**
 * Plaform configuration wrapper for Maven
 */
public class Platform {
	
	protected org.openqa.selenium.Platform platform;
	
	public Platform(org.openqa.selenium.Platform platform) {
		this.platform = platform;
	}
	
	public Platform(String platform) {
		this(org.openqa.selenium.Platform.valueOf(platform));
	}
	
	public org.openqa.selenium.Platform toPlatform() {
		return platform;
	}
}
