package com.sele3.asserts;

import java.time.Duration;
import java.util.function.Consumer;

import org.assertj.core.api.AbstractAssert;
import org.openqa.selenium.TimeoutException;

import com.sele3.elements.BaseElement;
import com.sele3.waits.ElementWait;

/**
 * AssertJ assertions for a {@link BaseElement}, obtained via {@link Assert#assertThat(BaseElement)}
 * or {@link SoftAssert#assertThat(BaseElement)}. Each check polls the element (via
 * {@link BaseElement#waits()}, i.e. {@link ElementWait}) up to the current driver's configured
 * timeout, or the one set with {@link #withTimeout}, instead of checking its state once.
 *
 * <p>Not final: {@link SoftAssert} subclasses it at runtime to collect failures instead of throwing.
 */
public class ElementAssert extends AbstractAssert<ElementAssert, BaseElement> {

    private Duration timeout;

    /**
     * Creates assertions for the given element. Public because {@link SoftAssert} instantiates it
     * reflectively.
     *
     * @param actual the element under assertion
     */
    public ElementAssert(BaseElement actual) {
        super(actual, ElementAssert.class);
    }

    /**
     * Overrides the configured timeout for the checks that follow in this chain, e.g.
     * {@code assertThat(el).withTimeout(Duration.ofSeconds(5)).isVisible()}.
     *
     * @param timeout the maximum time each check waits; {@code null} uses the configured timeout
     * @return this assertion object
     */
    public ElementAssert withTimeout(Duration timeout) {
        this.timeout = timeout;
        return myself;
    }

    /**
     * Asserts that an element matching the locator is present in the DOM.
     *
     * @return this assertion object
     */
    public ElementAssert isAttached() {
        return check(ElementWait::untilExist, "element to be attached to the DOM");
    }

    /**
     * Asserts that an element matching the locator is present and visible.
     *
     * @return this assertion object
     */
    public ElementAssert isVisible() {
        return check(ElementWait::untilVisible, "element to be visible");
    }

    /**
     * Asserts that an element matching the locator is present in the DOM but not displayed.
     *
     * @return this assertion object
     */
    public ElementAssert isHidden() {
        return check(ElementWait::untilInvisible, "element to be hidden");
    }

    /**
     * Asserts that the element is visible and enabled.
     *
     * @return this assertion object
     */
    public ElementAssert isInteractable() {
        return check(ElementWait::untilClickable, "element to be interactable");
    }

    /**
     * Asserts that the element is enabled.
     *
     * @return this assertion object
     */
    public ElementAssert isEnabled() {
        return check(ElementWait::untilEnabled, "element to be enabled");
    }

    /**
     * Asserts that the element is disabled.
     *
     * @return this assertion object
     */
    public ElementAssert isDisabled() {
        return check(ElementWait::untilDisabled, "element to be disabled");
    }

    /**
     * Asserts that the element is selected/checked.
     *
     * @return this assertion object
     */
    public ElementAssert isChecked() {
        return check(ElementWait::untilChecked, "element to be checked");
    }

    /**
     * Asserts that the element is deselected/unchecked.
     *
     * @return this assertion object
     */
    public ElementAssert isUnchecked() {
        return check(ElementWait::untilUnchecked, "element to be unchecked");
    }

    /**
     * Asserts that the element's visible text equals {@code text}.
     *
     * @param text the expected text
     * @return this assertion object
     */
    public ElementAssert hasText(String text) {
        return check(w -> w.untilTextEquals(text), "element text to equal " + quote(text));
    }

    /**
     * Asserts that the element's visible text does not equal {@code text}.
     *
     * @param text the text expected to no longer match
     * @return this assertion object
     */
    public ElementAssert doesNotHaveText(String text) {
        return check(w -> w.untilTextNotEquals(text), "element text to not equal " + quote(text));
    }

    /**
     * Asserts that the element's visible text contains {@code text}.
     *
     * @param text the substring expected to appear in the element's text
     * @return this assertion object
     */
    public ElementAssert containsText(String text) {
        return check(w -> w.untilTextContains(text), "element text to contain " + quote(text));
    }

    /**
     * Asserts that the element's {@code value} attribute equals {@code value}.
     *
     * @param value the expected value
     * @return this assertion object
     */
    public ElementAssert hasValue(String value) {
        return check(w -> w.untilValueEquals(value), "element value to equal " + quote(value));
    }

    /**
     * Asserts that the element's {@code value} attribute does not equal {@code value}.
     *
     * @param value the value expected to no longer match
     * @return this assertion object
     */
    public ElementAssert doesNotHaveValue(String value) {
        return check(w -> w.untilValueNotEquals(value), "element value to not equal " + quote(value));
    }

    /**
     * Asserts that the element's {@code value} attribute contains {@code value}.
     *
     * @param value the substring expected to appear in the element's value
     * @return this assertion object
     */
    public ElementAssert containsValue(String value) {
        return check(w -> w.untilValueContains(value), "element value to contain " + quote(value));
    }

    /**
     * Asserts that the given attribute on the element equals {@code value}.
     *
     * @param attribute the attribute name
     * @param value the expected value
     * @return this assertion object
     */
    public ElementAssert hasAttribute(String attribute, String value) {
        return check(w -> w.untilAttributeEquals(attribute, value),
                "attribute " + quote(attribute) + " to equal " + quote(value));
    }

    /**
     * Asserts that the given attribute on the element does not equal {@code value}.
     *
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     * @return this assertion object
     */
    public ElementAssert doesNotHaveAttribute(String attribute, String value) {
        return check(w -> w.untilAttributeNotEquals(attribute, value),
                "attribute " + quote(attribute) + " to not equal " + quote(value));
    }

    /**
     * Asserts that the given attribute on the element contains {@code value}.
     *
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     * @return this assertion object
     */
    public ElementAssert containsAttribute(String attribute, String value) {
        return check(w -> w.untilAttributeContains(attribute, value),
                "attribute " + quote(attribute) + " to contain " + quote(value));
    }

    /**
     * Runs {@code waitAction} (an {@link ElementWait} {@code until*} call) on a fresh wait and
     * fails if it times out; a {@code null} element fails immediately.
     */
    private ElementAssert check(Consumer<ElementWait> waitAction, String expectation) {
        isNotNull();
        ElementWait wait = actual.waits().withTimeout(timeout);
        try {
            waitAction.accept(wait);
        } catch (TimeoutException e) {
            failWithMessage("Expecting %s (waited up to %d ms)", expectation, wait.getTimeout().toMillis());
        }
        return myself;
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
