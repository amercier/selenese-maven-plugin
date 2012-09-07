package com.github.amercier.selenium.selenese;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.github.amercier.selenium.exceptions.InvalidSeleneseCommandArgumentException;

/**
 * Option locators.
 * See {@link http://release.seleniumhq.org/selenium-core/1.0.1/reference.html#select}
 */
public enum OptionLocator {
	
	REGEXP(Pattern.compile("^label=regexp:(.*)$")),
	LABEL (Pattern.compile("^label=(.*)$")),
	VALUE (Pattern.compile("^value=(.*)$")),
	INDEX (Pattern.compile("^index=([0-9]*)$")),
	ID    (Pattern.compile("^id=(.*)$"));
	
	private final Pattern pattern;
	private final int     group;
	
	private OptionLocator(Pattern pattern, int group) {
		this.pattern = pattern;
		this.group = group;
	}
	
	private OptionLocator(Pattern pattern) {
		this(pattern, 1);
	}
	
	public String find(CharSequence input) {
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(group) : null;
	}
	
	public static By parse(String seleneseSelector) throws InvalidSeleneseCommandArgumentException {
		String matched;
		for(OptionLocator locator : OptionLocator.values()) {
			if((matched = locator.find(seleneseSelector)) != null) {
				switch(locator) {
					case ID    : return By.xpath("//option[@id=\"" + matched + "\"]");
					case LABEL : return By.xpath("//option[text()=\"" + matched + "\"]");
					case VALUE : return By.xpath("//option[@value=\"" + matched + "\"]");
					case INDEX : return By.xpath("//option[" + matched + "]");
					case REGEXP:
						final Pattern pattern = Pattern.compile(matched);
						return new By() {
							@Override
							public List<WebElement> findElements(SearchContext context) {
								List<WebElement> elements = new LinkedList<WebElement>();
								for(WebElement element : context.findElements(By.xpath("option"))) {
									if(pattern.matcher(element.getText()).find()) {
										elements.add(element);
									}
								}
								return elements;
							}
							
						};
				}
			}
		}
		throw new InvalidSeleneseCommandArgumentException(seleneseSelector);
	}
}
