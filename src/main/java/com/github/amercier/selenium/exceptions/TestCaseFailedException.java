package com.github.amercier.selenium.exceptions;

import com.github.amercier.selenium.selenese.SeleneseTestCase;

@SuppressWarnings("serial")
public class TestCaseFailedException extends Exception {
	
	protected String reason;

	public TestCaseFailedException(SeleneseTestCase testCase, String reason) {
		super(reason);
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
