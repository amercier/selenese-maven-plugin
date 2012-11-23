package com.github.amercier.selenium.selenese;


public enum Action {
	
	assertLocation(1),
	assertElementPresent(1),
	assertElementNotPresent(1),
	assertText(2),
	check(1),
	click(1),
	dragAndDropToObject(2),
	echo(1),
	getEval(1),
	open(1),
	pause(1),
	select(2),
	storeEval(2),
	type(2),
	waitForElementNotPresent(1),
	waitForElementPresent(1);
	
	private final int argumentsCount;
	
	private Action(int argumentsCount) {
		this.argumentsCount = argumentsCount;
	}
	
	public int getArgumentsCount() {
		return argumentsCount;
	}
}
