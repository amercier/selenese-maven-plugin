package com.github.amercier.selenium.selenese;


public enum Action {
	
	open(1),
	type(2),
	click(1),
	pause(1),
	assertLocation(1),
	assertElementPresent(1),
	storeEval(2);
	
	private final int argumentsCount;
	
	private Action(int argumentsCount) {
		this.argumentsCount = argumentsCount;
	}
	
	public int getArgumentsCount() {
		return argumentsCount;
	}
}
