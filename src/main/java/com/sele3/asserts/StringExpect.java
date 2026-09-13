package com.sele3.asserts;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent matchers for a {@link String} value, obtained via {@link Assert#expect} or
 * {@link SoftAssert#expect}.
 */
public final class StringExpect extends BaseExpect<String> {

    StringExpect(String actual, String description, Consumer<AssertionException> onFailure) {
        super(actual, description, onFailure);
    }

    /**
     * Asserts that the actual string is equal to {@code expected}.
     *
     * @param expected the expected string
     */
    public void toEqual(String expected) {
        check(Objects.equals(actual, expected), "Expected: to equal " + format(expected));
    }

    /**
     * Asserts that the actual string is equal to {@code expected}, ignoring case.
     *
     * @param expected the expected string
     */
    public void toEqualIgnoringCase(String expected) {
        check(actual != null && actual.equalsIgnoreCase(expected), "Expected: to equal (ignoring case) " + format(expected));
    }

    /**
     * Asserts that the actual string contains {@code expected} as a substring.
     *
     * @param expected the substring expected to be contained
     */
    public void toContain(CharSequence expected) {
        check(actual != null && actual.contains(expected), "Expected: to contain " + format(expected));
    }

    /**
     * Asserts that the actual string starts with {@code expected}.
     *
     * @param expected the expected prefix
     */
    public void toStartWith(String expected) {
        check(actual != null && actual.startsWith(expected), "Expected: to start with " + format(expected));
    }

    /**
     * Asserts that the actual string ends with {@code expected}.
     *
     * @param expected the expected suffix
     */
    public void toEndWith(String expected) {
        check(actual != null && actual.endsWith(expected), "Expected: to end with " + format(expected));
    }

    /**
     * Asserts that the actual string matches the given regular expression, per {@link String#matches}.
     *
     * @param regex the regular expression the actual string must fully match
     */
    public void toMatch(String regex) {
        check(actual != null && actual.matches(regex), "Expected: to match pattern " + format(regex));
    }

    /**
     * Asserts that the actual string is empty.
     */
    public void toBeEmpty() {
        check(actual != null && actual.isEmpty(), "Expected: to be empty");
    }

    /**
     * Asserts that the actual string is not empty.
     */
    public void toBeNotEmpty() {
        check(actual != null && !actual.isEmpty(), "Expected: to be non-empty");
    }

    /**
     * Asserts that the actual string is {@code null}.
     */
    public void toBeNull() {
        check(actual == null, "Expected: to be null");
    }

    /**
     * Asserts that the actual string is not {@code null}.
     */
    public void toBeNotNull() {
        check(actual != null, "Expected: to be non-null");
    }
}
