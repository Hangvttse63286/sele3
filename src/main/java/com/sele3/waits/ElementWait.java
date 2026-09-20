package com.sele3.waits;

import java.time.Duration;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SeleniumWait} plus convenience waits bound to a single {@link #element}'s locator
 * (existence, visibility, text, attributes, enabled/disabled state, {@code <select>} options,
 * etc.). See {@link BaseElement#waits()} for the usual way to obtain one.
 */
@Slf4j
@Getter
public class ElementWait extends SeleniumWait {
    private BaseElement element;

    /**
     * Creates a {@link ElementWait} bound to {@code element}, using the current driver's
     * configured timeout and polling interval.
     *
     * @param element the element that the element-specific waits (e.g. {@link #exist}, {@link #visible}) act on
     */
    public ElementWait(BaseElement element) {
        super();
        this.element = element;
    }

    /**
     * Creates a {@link ElementWait} bound to {@code element}, using the given timeout and
     * polling interval instead of the driver's configured defaults.
     *
     * @param element the element that the element-specific waits (e.g. {@link #exist}, {@link #visible}) act on
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to re-evaluate the condition while waiting
     */
    public ElementWait(BaseElement element, Duration timeout, Duration pollingInterval) {
        super(timeout, pollingInterval);
        this.element = element;
    }

    /**
     * Locates the element matching this wait's locator. Used for conditions that can't be
     * expressed with {@link ExpectedConditions} (e.g. {@link #untilTextNotEquals},
     * {@link #untilAttributeNotEquals}, {@link #untilUnchecked}).
     *
     * @return the located element
     */
    private WebElement findElement() {
        return DriverRunner.getWebDriver().findElement(getLocator());
    }

    /**
     * Gets the locator of this wait's bound {@link #element}, re-read on every call so it
     * reflects the current locator if {@link BaseElement#set(Object...)} was called since this
     * wait was created.
     *
     * @return the current locator
     */
    private By getLocator() {
        return element.getLocator();
    }

    /**
     * Waits until an element matching the locator is present in the DOM.
     */
    public void untilExist() {
        until(ExpectedConditions.presenceOfElementLocated(getLocator()));
    }

    /**
     * Waits until at least one element matching the locator is present in the DOM.
     */
    public void untilAllExist() {
        until(ExpectedConditions.presenceOfAllElementsLocatedBy(getLocator()));
    }

    /**
     * Waits until an element matching the locator is present and visible.
     */
    public void untilVisible() {
        until(ExpectedConditions.visibilityOfElementLocated(getLocator()));
    }

    /**
     * Waits until all elements matching the locator are present and visible.
     */
    public void untilAllVisible() {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(getLocator()));
    }

    /**
     * Waits until every element matching the locator is invisible (or no element matches at
     * all).
     */
    public void untilInvisible() {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(driver -> driver.findElements(getLocator()).stream().noneMatch(WebElement::isDisplayed));
    }

    /**
     * Waits until the given element is enabled.
     */
    public void untilEnabled() {
        untilClickable();
    }

    /**
     * Waits until the given element is disabled.
     */
    public void untilDisabled() {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(driver -> !findElement().isEnabled());
    }

    /**
     * Waits until an element matching the locator is visible and enabled.
     */
    public void untilClickable() {
        until(ExpectedConditions.elementToBeClickable(getLocator()));
    }

    /**
     * Waits until the element's {@code value} attribute equals the given value.
     *
     * @param value the expected value
     */
    public void untilValueEquals(String value) {
        untilAttributeEquals("value", value);
    }

    /**
     * Waits until the element's {@code value} attribute no longer equals the given value.
     *
     * @param value the value expected to no longer match
     */
    public void untilValueNotEquals(String value) {
        untilAttributeNotEquals("value", value);
    }

    /**
     * Waits until the element's {@code value} attribute contains the given text.
     *
     * @param value the substring expected to appear in the value
     */
    public void untilValueContains(String value) {
        untilAttributeContains("value", value);
    }

    /**
     * Waits until the element's visible text equals the given text.
     *
     * @param text the expected text
     */
    public void untilTextEquals(String text) {
        until(ExpectedConditions.textToBe(getLocator(), text));
    }

    /**
     * Waits until the element's visible text no longer equals the given text.
     *
     * @param text the text expected to no longer match
     */
    public void untilTextNotEquals(String text) {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(driver -> !findElement().getText().equals(text));
    }

    /**
     * Waits until the element matching the locator contains the given text.
     *
     * @param text the substring expected to appear in the element's text
     */
    public void untilTextContains(String text) {
        until(ExpectedConditions.textToBePresentInElementLocated(getLocator(), text));
    }

    /**
     * Waits until the given attribute on the element equals the given value.
     *
     * @param attribute the attribute name
     * @param value the expected value
     */
    public void untilAttributeEquals(String attribute, String value) {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(ExpectedConditions.attributeToBe(getLocator(), attribute, value));
    }

    /**
     * Waits until the given attribute on the element no longer equals the given value.
     *
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     */
    public void untilAttributeNotEquals(String attribute, String value) {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(driver -> !Objects.equals(findElement().getAttribute(attribute), value));
    }

    /**
     * Waits until the given attribute on the element contains the given value.
     *
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     */
    public void untilAttributeContains(String attribute, String value) {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(ExpectedConditions.attributeContains(getLocator(), attribute, value));
    }

    /**
     * Waits until the element matching this wait's locator contains an element matching the
     * child locator.
     *
     * @param childElement the child element whose presence is required
     */
    public void untilChildrenExist(BaseElement childElement) {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(ExpectedConditions.presenceOfNestedElementsLocatedBy(getLocator(), childElement.getLocator()));
    }

    /**
     * Waits until the element's selection/checked state matches {@code selected}. Backs
     * {@link #untilChecked()} and {@link #untilUnchecked()}.
     *
     * @param selected the expected selection state
     */
    public void untilSelectionStateToBe(boolean selected) {
        ignoreAll(RetryableExceptions.COMMON_EXCEPTIONS)
            .until(ExpectedConditions.elementSelectionStateToBe(getLocator(), selected));
    }

    /**
     * Waits until the given element is selected/checked.
     */
    public void untilChecked() {
        untilSelectionStateToBe(true);
    }

    /**
     * Waits until the given element is deselected/unchecked.
     */
    public void untilUnchecked() {
        untilSelectionStateToBe(false);
    }

    /**
     * Waits until the number of elements matching the locator equals the given count.
     *
     * @param expectedCount the expected number of elements
     */
    public void untilElementsAreExactly(int expectedCount) {
        until(driver -> getElement().getSize() == expectedCount);
    }

    /**
     * Waits until the number of elements matching the locator is at least the given count.
     *
     * @param expectedCount the minimum expected number of elements
     */
    public void untilElementsAreAtLeast(int expectedCount) {
        until(driver -> getElement().getSize() >= expectedCount);
    }
}
