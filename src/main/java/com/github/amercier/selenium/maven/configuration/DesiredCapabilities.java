package com.github.amercier.selenium.maven.configuration;

import org.openqa.selenium.Platform;

import com.github.amercier.selenium.exceptions.UnknownPlatformException;

/**
 * DesiredCapabilities configuration wrapper for Maven:
 * 
 * Firefox (any version, any platform):
 * 
 *     <desiredCapabilities>
 *         <browser>firefox</browser>
 *     </desiredCapabilities>
 *     
 * Firefox 3.6 on Windows:
 * 
 *     <desiredCapabilities>
 *         <browser>firefox</browser>
 *         <version>3.6</browser
 *         <platform>WINDOWS</platform>
 *     </desiredCapabilities>
 */
public class DesiredCapabilities {

	/**
	 * @parameter
	 * @required
	 */
	public String browser = null;
	
	/**
	 * @parameter
	 */
	public String version = null;
	
	/**
	 * @parameter
	 */
	public String platform = null;
	
	/**
	 * Platfof
	 * @param platform The platform. Must be one of the {@link Platform possible values} 
	 * @throws UnknownPlatformException If the platform is invalid
	 */
	public void setPlatform(String platform) throws UnknownPlatformException {
		try {
			org.openqa.selenium.Platform.valueOf(platform);
			this.platform = platform;
		}
		catch(Exception e) {
			this.platform = null;
			throw new UnknownPlatformException(platform);
		}
	}
	
	/**
	 * Convert into a {@link org.openqa.selenium.remote.DesiredCapabilities Selenium DesiredCapabilities} object
	 * @return Returns the {@link org.openqa.selenium.remote.DesiredCapabilities Selenium DesiredCapabilities} equivalent object
	 */
	public org.openqa.selenium.remote.DesiredCapabilities toCapabilities() {
		return new org.openqa.selenium.remote.DesiredCapabilities(
				browser  == null ? "" : browser,
				version  == null ? "" : version,
				platform == null ? org.openqa.selenium.Platform.ANY : org.openqa.selenium.Platform.valueOf(platform)
			);
	}
	
	/**
	 * Convert into a human-readable String:
	 * 
	 *     ( browser | "Any browser" ) [version] ["on" platform] 
	 */
	public String toString() {
		return (browser  == null ? "Any browser" : browser)
			+ (version  == null ? "" : " " + version)
			+ (platform  == null ? "" : " on " + platform);
	}
}
