package com.sele3.asserts;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent matchers for an arbitrary value, obtained via {@link Assert#expect} or
 * {@link SoftAssert#expect}.
 *
 * @param <T> the type of the value under assertion
 */
public class ObjectExpect<T> extends BaseExpect<T> {

    ObjectExpect(T actual, String description, Consumer<AssertionException> onFailure) {
        super(actual, description, onFailure);
    }

    /**
     * Asserts that the actual value is equal to {@code expected}, per {@link Objects#equals}.
     *
     * @param expected the expected value
     */
    public void toEqual(T expected) {
        check(Objects.equals(actual, expected), "Expected: to equal " + format(expected));
    }

    /**
     * Asserts that the actual value is not equal to {@code expected}, per {@link Objects#equals}.
     *
     * @param expected the value the actual value must differ from
     */
    public void toNotEqual(T expected) {
        check(!Objects.equals(actual, expected), "Expected: to not equal " + format(expected));
    }

    /**
     * Asserts that the actual value is {@code null}.
     */
    public void toBeNull() {
        check(actual == null, "Expected: to be null");
    }

    /**
     * Asserts that the actual value is not {@code null}.
     */
    public void toBeNotNull() {
        check(actual != null, "Expected: to be non-null");
    }

    /**
     * Asserts that the actual value is the same object instance as {@code expected} (reference
     * equality, {@code ==}).
     *
     * @param expected the instance the actual value must be identical to
     */
    public void toBeSameAs(T expected) {
        check(actual == expected, "Expected: to be the same instance as " + format(expected));
    }
}
