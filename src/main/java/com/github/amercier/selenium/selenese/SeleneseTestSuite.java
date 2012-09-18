package com.github.amercier.selenium.selenese;

import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

public class SeleneseTestSuite extends JUnitTest { // TODO Move dependency with JUnitTest

	//protected String name;
	protected List<SeleneseTestCase> testCases;
	
	public SeleneseTestSuite(String name) {
		super(name);
		//this.setName(name);
		testCases = new LinkedList<SeleneseTestCase>();
	}
	
	/*
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	*/
	public void addTestCase(SeleneseTestCase testCase) {
		this.testCases.add(testCase);
	}
	
	public SeleneseTestCase[] getTestCases() {
		return testCases.toArray(new SeleneseTestCase[0]);
	}
}
