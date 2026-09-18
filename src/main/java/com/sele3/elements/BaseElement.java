package com.sele3.elements;

import java.util.List;

import org.openqa.selenium.By;

import com.sele3.waits.ElementWait;

/**
 * Contract for page-object elements: wraps a {@link By} locator (fixed or resolved from a
 * dynamic XPath template) and exposes wait-aware accessors/actions on it. The current-thread
 * WebDriver must be initialized before invoking methods that access the browser, including
 * {@link #waits()}.
 */
public interface BaseElement {

    /**
     * Resolves this element's dynamic XPath template by substituting the given arguments and
     * replacing the current locator.
     *
     * @param args the values to substitute into the XPath template
     */
    void set(Object... args);

    /**
     * Gets the locator currently used to find this element.
     *
     * @return the current locator
     */
    By getLocator();

    /**
     * Gets the value of the given attribute or property.
     *
     * @param attribute the attribute/property name
     * @return the attribute's value, or {@code null} if not present
     */
    String getAttribute(String attribute);

    /**
     * Gets the visible (rendered) text of this element.
     *
     * @return the element's visible text
     */
    String getText();

    /**
     * Gets this element's {@code innerText} property.
     *
     * @return the element's {@code innerText}
     */
    String getInnerText();

    /**
     * Gets this element's {@code value} attribute.
     *
     * @return the element's {@code value}
     */
    String getValue();

    /**
     * Gets the computed value of a CSS property on this element.
     *
     * @param name the CSS property name
     * @return the computed CSS value
     */
    String getCssValue(String name);

    /**
     * Gets the computed value of a CSS property for every matching element.
     *
     * @param name the CSS property name
     * @return the computed CSS value for each matching element, in DOM order
     */
    List<String> getAllCssValues(String name);

    /**
     * Gets the visible text of every matching element.
     *
     * @return the visible text of each matching element, in DOM order
     */
    List<String> getAllTexts();

    /**
     * Scrolls the element into view, aligning it to the top of the viewport.
     */
    void scrollToView();

    /**
     * Scrolls the element to the center of the viewport and left-clicks it.
     */
    void click();

    /**
     * Scrolls the element to the center of the viewport and right-clicks it.
     */
    void rightClick();

    /**
     * Scrolls the element to the center of the viewport and double-clicks it.
     */
    void doubleClick();

    /**
     * Scrolls the element to the center of the viewport and hovers the mouse over it.
     */
    void hover();

    /**
     * Clears this element's current value, then types the given character sequences into it.
     *
     * @param values the character sequences to send
     */
    void clearAndEnter(CharSequence... values);

    /**
     * Types the given character sequences into this element, without clearing its current value first.
     *
     * @param values the character sequences to send
     */
    void enter(CharSequence... values);

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its visible text.
     *
     * @param option the visible text of the option to select
     */
    void select(String option);

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its {@code value} attribute.
     *
     * @param value the {@code value} attribute of the option to select
     */
    void selectByValue(String value);

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its index.
     *
     * @param index the zero-based index of the option to select
     */
    void selectByIndex(int index);

    /**
     * Gets the visible text of the currently selected option in this {@code <select>} element.
     *
     * @return the selected option's visible text
     */
    String getSelectedOption();

    /**
     * Gets the visible text of every option in this {@code <select>} element.
     *
     * @return the visible text of each option, in DOM order
     */
    List<String> getAllSelectedOptions();

    /**
     * Scrolls the element to the center of the viewport and clicks it via JavaScript,
     * bypassing Selenium's native click (useful when the element is covered or not
     * otherwise clickable).
     */
    void clickViaJS();

    /**
     * Scrolls the element into the center of the viewport, both vertically and horizontally.
     */
    void scrollToCenter();

    /**
     * Creates and returns an {@link ElementWait} bound to this element and the current driver.
     * A new wait is created on each call, so the wait uses the driver and configuration active
     * when the method is invoked.
     *
     * @return this element's {@link ElementWait}
     */
    ElementWait waits();

    /**
     * Checks whether this element is currently enabled.
     *
     * @return {@code true} if the element is enabled
     */
    boolean isEnabled();

    /**
     * Checks whether this element is currently disabled.
     *
     * @return {@code true} if the element is disabled
     */
    boolean isDisabled();

    /**
     * Gets the number of matching elements in the DOM. This is equivalent to {@code findElements().size()}.
     * 
     * @return the number of matching elements
     */
    int getSize();

    /**
     * Checks whether this element is currently selected/checked.
     *
     * @return {@code true} if the element is selected
     */
    boolean isSelected();
}
