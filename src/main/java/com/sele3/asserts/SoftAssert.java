package com.sele3.asserts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sele3.elements.BaseElement;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for soft assertions, in the style of Playwright Test's {@code expect.soft()}:
 * {@code softAssert.expect(actual).toEqual(expected)} records a failure instead of throwing,
 * letting the rest of the test keep running; call {@link #assertAll()} to fail the test with every
 * collected failure reported together. For a fail-fast equivalent, see {@link Assert}.
 *
 * <p>Failures are kept in a {@link ThreadLocal}, so a single {@link SoftAssert} instance (e.g. a
 * shared field on a base test class) is safe to use from tests running in parallel on different
 * threads: each thread only ever sees and clears its own failures.
 *
 * <p>{@link #record(AssertionException)} is also this instance's hook for a custom matcher over
 * your own type; see {@link BaseExpect} for how to build one.
 */
@Slf4j
public class SoftAssert {
    private final ThreadLocal<List<AssertionException>> failures = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Starts a soft assertion on an arbitrary value.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <T> the type of the value under assertion
     * @return a matcher for {@code actual}
     */
    public <T> ObjectExpect<T> expect(T actual, String... description) {
        return Expects.object(actual, this::record, description);
    }

    /**
     * Starts a soft assertion on a {@code boolean}/{@link Boolean} value.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public BooleanExpect expect(Boolean actual, String... description) {
        return Expects.bool(actual, this::record, description);
    }

    /**
     * Starts a soft assertion on a {@link String}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @return a matcher for {@code actual}
     */
    public StringExpect expect(String actual, String... description) {
        return Expects.string(actual, this::record, description);
    }

    /**
     * Starts a soft assertion on a {@link Number} that is also {@link Comparable}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <T> the numeric type under assertion
     * @return a matcher for {@code actual}
     */
    public <T extends Number & Comparable<T>> NumberExpect<T> expect(T actual, String... description) {
        return Expects.number(actual, this::record, description);
    }

    /**
     * Starts a soft assertion on a {@link Collection}.
     *
     * @param actual the value under assertion
     * @param description an optional label prefixed to any resulting failure message
     * @param <E> the element type of the collection under assertion
     * @return a matcher for {@code actual}
     */
    public <E> CollectionExpect<E> expect(Collection<E> actual, String... description) {
        return Expects.collection(actual, this::record, description);
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
    public ElementExpect expect(BaseElement actual, String... description) {
        return Expects.element(actual, this::record, description);
    }

    /**
     * Records a failure with the given message, regardless of any condition, without stopping the
     * current test.
     *
     * @param message the failure message
     */
    public void fail(String message) {
        record(new AssertionException(message));
    }

    /**
     * Returns whether any soft assertion has failed on the current thread since the last
     * {@link #assertAll()}/{@link #reset()}.
     *
     * @return {@code true} if at least one failure is pending on the current thread
     */
    public boolean hasFailures() {
        return !failures.get().isEmpty();
    }

    /**
     * Returns the failures recorded on the current thread since the last
     * {@link #assertAll()}/{@link #reset()}, in the order they occurred.
     *
     * @return the current thread's pending failures
     */
    public List<AssertionException> getFailures() {
        return List.copyOf(failures.get());
    }

    /**
     * Discards any failures recorded on the current thread without throwing, e.g. to reuse a
     * shared instance for a fresh test on the same thread.
     */
    public void reset() {
        failures.remove();
    }

    /**
     * Throws a {@link SoftAssertionException} aggregating every failure recorded on the current
     * thread since the last {@link #assertAll()}/{@link #reset()}, then clears them; does nothing
     * if none are pending.
     *
     * @throws SoftAssertionException if any soft assertion failed on the current thread
     */
    public void assertAll() {
        List<AssertionException> collected = getFailures();
        reset();
        if (!collected.isEmpty()) {
            throw new SoftAssertionException(collected);
        }
    }

    /**
     * Records {@code error} on the current thread without throwing. This is {@link SoftAssert}'s
     * failure handler for the built-in matchers ({@code this::record} is passed as their
     * {@code Consumer<AssertionException>}); a custom matcher built on {@link BaseExpect} can wire
     * its own soft-assert entry point to a {@link SoftAssert} instance's {@code ::record} to
     * collect into that same instance's thread-bound failure list.
     *
     * @param error the failure to record
     */
    public void record(AssertionException error) {
        log.warn("Soft assertion failed: {}", error.getMessage());
        failures.get().add(error);
    }
}
