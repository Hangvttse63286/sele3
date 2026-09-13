package com.sele3.asserts;

import java.util.function.Consumer;

import org.openqa.selenium.TimeoutException;

import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;
import com.sele3.waits.ElementWait;

/**
 * Fluent matchers for a {@link BaseElement}, obtained via {@link Assert#expect} or
 * {@link SoftAssert#expect}. Unlike the other {@code *Expect} types, each matcher here polls the
 * element (via {@link BaseElement#waits()}, i.e. {@link ElementWait}, using the current driver's
 * configured timeout/polling interval) instead of checking its state once — in the style of
 * Playwright Test's "web-first assertions" (e.g. {@code expect(locator).toBeVisible()}) — so a
 * slow-to-render element doesn't need a separate explicit wait before the assertion.
 */
public final class ElementExpect extends BaseExpect<BaseElement> {

    ElementExpect(BaseElement actual, String description, Consumer<AssertionException> onFailure) {
        super(actual, description, onFailure);
    }

    /**
     * Asserts that an element matching the locator is present in the DOM.
     */
    public void toBeAttached() {
        checkWait(() -> actual.waits().untilExist(), "Expected: element to be attached to the DOM");
    }

    /**
     * Asserts that an element matching the locator is present and visible.
     */
    public void toBeVisible() {
        checkWait(() -> actual.waits().untilVisible(), "Expected: element to be visible");
    }

    /**
     * Asserts that no element matching the locator is visible (or it is no longer present).
     */
    public void toBeHidden() {
        checkWait(() -> actual.waits().untilInvisible(), "Expected: element to be hidden");
    }

    /**
     * Asserts that the element is visible and enabled.
     */
    public void toBeClickable() {
        checkWait(() -> actual.waits().untilClickable(), "Expected: element to be clickable");
    }

    /**
     * Asserts that the element is enabled.
     */
    public void toBeEnabled() {
        checkWait(() -> actual.waits().untilEnabled(), "Expected: element to be enabled");
    }

    /**
     * Asserts that the element is disabled.
     */
    public void toBeDisabled() {
        checkWait(() -> actual.waits().untilDisabled(), "Expected: element to be disabled");
    }

    /**
     * Asserts that the element is selected/checked.
     */
    public void toBeChecked() {
        checkWait(() -> actual.waits().untilChecked(), "Expected: element to be checked");
    }

    /**
     * Asserts that the element is deselected/unchecked.
     */
    public void toBeUnchecked() {
        checkWait(() -> actual.waits().untilUnchecked(), "Expected: element to be unchecked");
    }

    /**
     * Asserts that the element's visible text equals {@code text}.
     *
     * @param text the expected text
     */
    public void toHaveText(String text) {
        checkWait(() -> actual.waits().untilTextEquals(text), "Expected: element text to equal " + format(text));
    }

    /**
     * Asserts that the element's visible text does not equal {@code text}.
     *
     * @param text the text expected to no longer match
     */
    public void toNotHaveText(String text) {
        checkWait(() -> actual.waits().untilTextNotEquals(text), "Expected: element text to not equal " + format(text));
    }

    /**
     * Asserts that the element's visible text contains {@code text}.
     *
     * @param text the substring expected to appear in the element's text
     */
    public void toContainText(String text) {
        checkWait(() -> actual.waits().untilTextContains(text), "Expected: element text to contain " + format(text));
    }

    /**
     * Asserts that the element's {@code value} attribute equals {@code value}.
     *
     * @param value the expected value
     */
    public void toHaveValue(String value) {
        checkWait(() -> actual.waits().untilValueEquals(value), "Expected: element value to equal " + format(value));
    }

    /**
     * Asserts that the element's {@code value} attribute does not equal {@code value}.
     *
     * @param value the value expected to no longer match
     */
    public void toNotHaveValue(String value) {
        checkWait(() -> actual.waits().untilValueNotEquals(value), "Expected: element value to not equal " + format(value));
    }

    /**
     * Asserts that the element's {@code value} attribute contains {@code value}.
     *
     * @param value the substring expected to appear in the element's value
     */
    public void toContainValue(String value) {
        checkWait(() -> actual.waits().untilValueContains(value), "Expected: element value to contain " + format(value));
    }

    /**
     * Asserts that the given attribute on the element equals {@code value}.
     *
     * @param attribute the attribute name
     * @param value the expected value
     */
    public void toHaveAttribute(String attribute, String value) {
        checkWait(() -> actual.waits().untilAttributeEquals(attribute, value),
                "Expected: attribute " + format(attribute) + " to equal " + format(value));
    }

    /**
     * Asserts that the given attribute on the element does not equal {@code value}.
     *
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     */
    public void toNotHaveAttribute(String attribute, String value) {
        checkWait(() -> actual.waits().untilAttributeNotEquals(attribute, value),
                "Expected: attribute " + format(attribute) + " to not equal " + format(value));
    }

    /**
     * Asserts that the given attribute on the element contains {@code value}.
     *
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     */
    public void toContainAttribute(String attribute, String value) {
        checkWait(() -> actual.waits().untilAttributeContains(attribute, value),
                "Expected: attribute " + format(attribute) + " to contain " + format(value));
    }

    /**
     * Runs {@code waitAction} (an {@link ElementWait} {@code until*} call) and reports failure via
     * {@link #check} if it times out; {@code actual} being {@code null} is treated as an immediate
     * failure rather than a {@link NullPointerException}.
     *
     * @param waitAction the polling wait to run, re-evaluating the condition until it holds or the timeout elapses
     * @param expectation a human-readable description of what was expected
     */
    private void checkWait(Runnable waitAction, String expectation) {
        boolean passed;
        if (actual == null) {
            passed = false;
        } else {
            try {
                waitAction.run();
                passed = true;
            } catch (TimeoutException e) {
                passed = false;
            }
        }
        check(passed, expectation + " (waited up to " + DriverRunner.getConfig().getTimeout() + ")");
    }
}
