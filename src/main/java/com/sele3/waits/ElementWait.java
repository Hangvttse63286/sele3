package com.sele3.waits;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.sele3.elements.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SeleniumWait} plus convenience waits bound to a single {@link #element}'s locator
 * (existence, visibility, text, attributes, enabled/disabled state, {@code <select>} options,
 * etc.). See {@link BaseElement#waits()} for the usual way to obtain one.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
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
     * Waits until an element matching the locator is present in the DOM.
     */
    public void untilExist() {
        until(ExpectedConditions.presenceOfElementLocated(getElement().getLocator()));
    }

    /**
     * Waits until at least one element matching the locator is present in the DOM.
     */
    public void untilAllExist() {
        until(ExpectedConditions.presenceOfAllElementsLocatedBy(getElement().getLocator()));
    }

    /**
     * Waits until an element matching the locator is present and visible.
     */
    public void untilVisible() {
        until(ExpectedConditions.visibilityOfElementLocated(getElement().getLocator()));
    }

    /**
     * Waits until all elements matching the locator are present and visible.
     */
    public void untilAllVisible() {
        until(ExpectedConditions.visibilityOfAllElementsLocatedBy(getElement().getLocator()));
    }

    /**
     * Waits until no element matching the locator is visible (or it is no longer present).
     */
    public void untilInvisible() {
        until(ExpectedConditions.invisibilityOfElementLocated(getElement().getLocator()));
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
        until(driver -> !getElement().isEnabled());
    }

    /**
     * Waits until an element matching the locator is visible and enabled.
     */
    public void untilClickable() {
        until(ExpectedConditions.elementToBeClickable(getElement().getLocator()));
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
        until(ExpectedConditions.textToBe(getElement().getLocator(), text));
    }

    /**
     * Waits until the element's visible text no longer equals the given text.
     *
     * @param text the text expected to no longer match
     */
    public void untilTextNotEquals(String text) {
        until(ExpectedConditions.not(ExpectedConditions.textToBe(getElement().getLocator(), text)));
    }

    /**
     * Waits until the element matching the locator contains the given text.
     *
     * @param text the substring expected to appear in the element's text
     */
    public void untilTextContains(String text) {
        until(ExpectedConditions.textToBePresentInElementLocated(getElement().getLocator(), text));
    }

    /**
     * Waits until the given attribute on the element equals the given value.
     *
     * @param attribute the attribute name
     * @param value the expected value
     */
    public void untilAttributeEquals(String attribute, String value) {
        until(ExpectedConditions.attributeToBe(getElement().getLocator(), attribute, value));
    }

    /**
     * Waits until the given attribute on the element no longer equals the given value.
     *
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     */
    public void untilAttributeNotEquals(String attribute, String value) {
        until(ExpectedConditions.not(ExpectedConditions.attributeToBe(getElement().getLocator(), attribute, value)));
    }

    /**
     * Waits until the given attribute on the element contains the given value.
     *
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     */
    public void untilAttributeContains(String attribute, String value) {
        until(ExpectedConditions.attributeContains(getElement().getLocator(), attribute, value));
    }

    /**
     * Waits until the {@code <select>} element matching the locator has its {@code <option>}
     * children populated.
     */
    public void untilSelectOptionsLoaded() {
        until(ExpectedConditions.presenceOfNestedElementsLocatedBy(getElement().getLocator(), By.tagName("option")));
    }

    /**
     * Waits until the given element is selected/checked.
     */
    public void untilChecked() {
        until(ExpectedConditions.elementToBeSelected(getElement().getLocator()));
    }

    /**
     * Waits until the given element is deselected/unchecked.
     */
    public void untilUnchecked() {
        until(ExpectedConditions.not(ExpectedConditions.elementToBeSelected(getElement().getLocator())));
    }
}
