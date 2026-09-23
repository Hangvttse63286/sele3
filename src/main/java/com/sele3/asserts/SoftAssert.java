package com.sele3.asserts;

import java.util.Collection;

import com.sele3.elements.BaseElement;

/**
 * Entry point for soft assertions: {@code SoftAssert.expect(actual).toEqual(expected)} records a
 * failure instead of throwing, letting the test keep running; call {@link #assertAll()} to fail
 * with every collected failure reported together. For a fail-fast equivalent, see {@link Assert}.
 *
 * <p>Delegates to a single shared, thread-safe {@link SoftAssertContainer} — nothing needs to be
 * instantiated to use it. Instantiate {@link SoftAssertContainer} directly only for an independent
 * soft-assert scope.
 */
public final class SoftAssert {
    private static final SoftAssertContainer softAssertContainer = new SoftAssertContainer();

    private SoftAssert() {
    }

    /**
     * Starts a soft assertion on an arbitrary value.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <T> the type of the value under assertion
     * @return a matcher for {@code actual}
     */
    public static <T> ObjectExpect<T> expect(T actual, String... description) {
        return softAssertContainer.expect(actual, description);
    }

    /**
     * Starts a soft assertion on a {@code boolean}/{@link Boolean} value.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public static BooleanExpect expect(Boolean actual, String... description) {
        return softAssertContainer.expect(actual, description);
    }

    /**
     * Starts a soft assertion on a {@link String}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public static StringExpect expect(String actual, String... description) {
        return softAssertContainer.expect(actual, description);
    }

    /**
     * Starts a soft assertion on a {@link Number} that is also {@link Comparable}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <T> the numeric type under assertion
     * @return a matcher for {@code actual}
     */
    public static <T extends Number & Comparable<T>> NumberExpect<T> expect(T actual, String... description) {
        return softAssertContainer.expect(actual, description);
    }

    /**
     * Starts a soft assertion on a {@link Collection}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <E> the element type of the collection under assertion
     * @return a matcher for {@code actual}
     */
    public static <E> CollectionExpect<E> expect(Collection<E> actual, String... description) {
        return softAssertContainer.expect(actual, description);
    }

    /**
     * Starts a soft assertion on a {@link BaseElement}. Unlike the other overloads, the returned
     * matcher polls the element up to the current driver's configured timeout before recording a
     * failure; see {@link ElementExpect}.
     *
     * @param actual the element under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public static ElementExpect expect(BaseElement actual, String... description) {
        return softAssertContainer.expect(actual, description);
    }

    /**
     * Records a failure with the given message, regardless of any condition, without stopping the
     * current test.
     *
     * @param message the failure message
     */
    public static void fail(String message) {
        softAssertContainer.fail(message);
    }

    /**
     * Throws a {@link SoftAssertionException} aggregating every failure recorded on the current
     * thread since the last {@link #assertAll()}, then clears them; does nothing if none are
     * pending.
     *
     * @throws SoftAssertionException if any soft assertion failed on the current thread
     */
    public static void assertAll() {
        softAssertContainer.assertAll();
    }
}
