package com.sele3.elements;

import org.openqa.selenium.By;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Element extends BaseElement {
    /**
     * Creates an Element from a dynamic XPath template, to be resolved later via {@link #set(Object...)}.
     *
     * @param dynamicXPathLocator an XPath template, e.g. {@code "//div[@id='%s']"}
     */
    public Element(String dynamicXPathLocator) {
        super(dynamicXPathLocator);
    }

    /**
     * Creates an Element from a fixed {@link By} locator.
     *
     * @param locator the locator used to find this element
     */
    public Element(By locator) {
        super(locator);
    }
}
