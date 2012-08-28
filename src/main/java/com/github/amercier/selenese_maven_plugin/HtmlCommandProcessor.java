package com.github.amercier.selenese_maven_plugin;

import com.thoughtworks.selenium.SeleniumException;

class HtmlCommandProcessor extends HttpCommandProcessor {
	final static String INDEX_SPECIFIER = "index=";
	final static String LABEL_SPECIFIER = "label=";
	final static String VALUE_SPECIFIER = "value=";

	boolean expectError;

	public HtmlCommandProcessor(String host, int port, String browser, String baseUrl) {
		super(host, port, browser, baseUrl);
	}

	public String doCommand(String cmd, String args[]) {
		if (cmd.equals("store")) {
			cmd += "Expression";
		} else if (cmd.equals("assertSelected") || cmd.equals("verifySelected")) {
			if (args[1].startsWith(INDEX_SPECIFIER)) {
				cmd += "Index";
				args[1] = args[1].substring(INDEX_SPECIFIER.length());
			} else if (args[1].startsWith(LABEL_SPECIFIER)) {
				cmd += "Label";
				args[1] = args[1].substring(LABEL_SPECIFIER.length());
			} else if (args[1].startsWith(VALUE_SPECIFIER)) {
				cmd += "Value";
				args[1] = args[1].substring(VALUE_SPECIFIER.length());
			} else {
				cmd += "Label";
			}
		} else if (cmd.endsWith("ErrorOnNext") || cmd.endsWith("FailureOnNext")) {
			expectError = true;
			return "OK";
		} else if (cmd.equals("echo")) {
			return "OK," + args[0];
		} else if (cmd.equals("pause")) {
			try {
				Thread.sleep(Integer.parseInt(args[0]));
				return "OK";
			} catch (InterruptedException e) {
				return "ERROR: pause interrupted";
			}
		}
		try {
			String result = super.doCommand(cmd, args);
			if (expectError) {
				throw new SeleniumException("ERROR: Error expected");
			} else {
				return result;
			}
		} catch (SeleniumException e) {
			if (expectError) {
				expectError = false;
				return "OK";
			} else {
				throw e;
			}
		}

	}
	
	@Override
	public String executeCommandOnServlet(String command) {
		System.out.println("Executing command on servlet: " + command);
		return super.executeCommandOnServlet(command);
	}
}
