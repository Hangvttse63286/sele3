package com.sele3.asserts;

import java.util.Collection;

import com.sele3.elements.BaseElement;

/**
 * Entry point for hard (fail-fast) assertions: {@code Assert.expect(actual).toEqual(expected)}
 * throws an {@link AssertionException} as soon as the assertion fails, immediately stopping the
 * current test. For an assertion that instead collects failures and reports them together, see
 * {@link SoftAssert}.
 */
public final class Assert {

    private Assert() {
    }

    /**
     * Starts an assertion on an arbitrary value.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <T> the type of the value under assertion
     * @return a matcher for {@code actual}
     */
    public static <T> ObjectExpect<T> expect(T actual, String... description) {
        return new ObjectExpect<>(actual, BaseExpect.describe(description), Assert::fail);
    }

    /**
     * Starts an assertion on a {@code boolean}/{@link Boolean} value.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public static BooleanExpect expect(Boolean actual, String... description) {
        return new BooleanExpect(actual, BaseExpect.describe(description), Assert::fail);
    }

    /**
     * Starts an assertion on a {@link String}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public static StringExpect expect(String actual, String... description) {
        return new StringExpect(actual, BaseExpect.describe(description), Assert::fail);
    }

    /**
     * Starts an assertion on a {@link Number} that is also {@link Comparable}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <T> the numeric type under assertion
     * @return a matcher for {@code actual}
     */
    public static <T extends Number & Comparable<T>> NumberExpect<T> expect(T actual, String... description) {
        return new NumberExpect<>(actual, BaseExpect.describe(description), Assert::fail);
    }

    /**
     * Starts an assertion on a {@link Collection}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <E> the element type of the collection under assertion
     * @return a matcher for {@code actual}
     */
    public static <E> CollectionExpect<E> expect(Collection<E> actual, String... description) {
        return new CollectionExpect<>(actual, BaseExpect.describe(description), Assert::fail);
    }

    /**
     * Starts an assertion on a {@link BaseElement}. Unlike the other overloads, the returned
     * matcher polls the element up to the current driver's configured timeout before failing; see
     * {@link ElementExpect}.
     *
     * @param actual the element under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public static ElementExpect expect(BaseElement actual, String... description) {
        return new ElementExpect(actual, BaseExpect.describe(description), Assert::fail);
    }

    /**
     * Fails immediately with the given message, regardless of any condition.
     *
     * @param message the failure message
     * @throws AssertionException always
     */
    public static void fail(String message) {
        throw new AssertionException(message);
    }

    /**
     * Throws {@code error} as-is. This is {@link Assert}'s failure handler for the built-in
     * matchers ({@code Assert::fail} is passed as their {@code Consumer<AssertionException>}); a
     * custom matcher built on {@link BaseExpect} can wire its own hard-assert entry point to this
     * same method reference to fail the same way.
     *
     * @param error the failure to throw
     * @throws AssertionException always, the given {@code error} itself
     */
    public static void fail(AssertionException error) {
        throw error;
    }
}
