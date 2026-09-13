package com.sele3.asserts;

import java.util.function.Consumer;

/**
 * Fluent matchers for a {@link Number} value that is also {@link Comparable} (e.g.
 * {@link Integer}, {@link Long}, {@link Double}, {@link java.math.BigDecimal}), obtained via
 * {@link Assert#expect} or {@link SoftAssert#expect}.
 *
 * @param <T> the numeric type under assertion
 */
public final class NumberExpect<T extends Number & Comparable<T>> extends BaseExpect<T> {

    NumberExpect(T actual, String description, Consumer<AssertionException> onFailure) {
        super(actual, description, onFailure);
    }

    /**
     * Asserts that the actual value is numerically equal to {@code expected}, per {@link Comparable#compareTo}.
     *
     * @param expected the expected value
     */
    public void toEqual(T expected) {
        check(actual != null && actual.compareTo(expected) == 0, "Expected: to equal " + format(expected));
    }

    /**
     * Asserts that the actual value is strictly greater than {@code expected}.
     *
     * @param expected the value the actual value must exceed
     */
    public void toBeGreaterThan(T expected) {
        check(actual != null && actual.compareTo(expected) > 0, "Expected: to be greater than " + format(expected));
    }

    /**
     * Asserts that the actual value is greater than or equal to {@code expected}.
     *
     * @param expected the lower bound, inclusive
     */
    public void toBeGreaterThanOrEqual(T expected) {
        check(actual != null && actual.compareTo(expected) >= 0, "Expected: to be greater than or equal to " + format(expected));
    }

    /**
     * Asserts that the actual value is strictly less than {@code expected}.
     *
     * @param expected the value the actual value must be below
     */
    public void toBeLessThan(T expected) {
        check(actual != null && actual.compareTo(expected) < 0, "Expected: to be less than " + format(expected));
    }

    /**
     * Asserts that the actual value is less than or equal to {@code expected}.
     *
     * @param expected the upper bound, inclusive
     */
    public void toBeLessThanOrEqual(T expected) {
        check(actual != null && actual.compareTo(expected) <= 0, "Expected: to be less than or equal to " + format(expected));
    }

    /**
     * Asserts that the actual value falls within {@code [min, max]}, inclusive.
     *
     * @param min the lower bound, inclusive
     * @param max the upper bound, inclusive
     */
    public void toBeBetween(T min, T max) {
        check(actual != null && actual.compareTo(min) >= 0 && actual.compareTo(max) <= 0,
                "Expected: to be between " + format(min) + " and " + format(max));
    }
}
