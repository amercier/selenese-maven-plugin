package com.github.amercier.selenium.exceptions;

import com.github.amercier.selenium.ServerAddress;
import com.github.amercier.selenium.maven.configuration.DesiredCapabilities;

@SuppressWarnings("serial")
public class CapabilitiesNotFoundException extends Exception {
	
	public DesiredCapabilities capabilities;
	
	public ServerAddress server;

	public CapabilitiesNotFoundException(DesiredCapabilities capabilities, ServerAddress server, String reason) {
		super(capabilities + " not found on server " + server + reason);
		this.capabilities = capabilities;
		this.server = server;
	}

	public CapabilitiesNotFoundException(DesiredCapabilities capabilities, ServerAddress server) {
		this(capabilities, server, "");
	}
}
