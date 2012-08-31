package com.github.amercier.selenium;

import java.net.InetSocketAddress;

@SuppressWarnings("serial")
public class ServerAddress extends InetSocketAddress {
	
	protected String host;
	
	public ServerAddress(String host, int port) {
		super(host, port);
		this.setHost(host);
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	@Override
	public String toString() {
		return getHost() + ":" + getPort();
	}

}
