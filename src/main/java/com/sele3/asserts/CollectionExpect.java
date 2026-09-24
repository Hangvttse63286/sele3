package com.sele3.asserts;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Fluent matchers for a {@link Collection}, obtained via {@link Assert#expect} or
 * {@link SoftAssert#expect}.
 * 
 * @param <E> the element type of the collection under assertion
 */
public final class CollectionExpect<E> extends ObjectExpect<Collection<E>> {

    CollectionExpect(Collection<E> actual, String description, Consumer<AssertionException> onFailure) {
        super(actual, description, onFailure);
    }

    /**
     * Asserts that the actual collection contains {@code expected}.
     *
     * @param expected the element expected to be present
     */
    public void toContain(E expected) {
        check(actual != null && actual.contains(expected), "Expected: to contain " + format(expected));
    }

    /**
     * Asserts that the actual collection has exactly {@code size} elements.
     *
     * @param size the expected element count
     */
    public void toHaveSize(int size) {
        check(actual != null && actual.size() == size, "Expected: to have size " + size);
    }

    /**
     * Asserts that the actual collection is empty.
     */
    public void toBeEmpty() {
        check(actual != null && actual.isEmpty(), "Expected: to be empty");
    }

    /**
     * Asserts that the actual collection is not empty.
     */
    public void toBeNotEmpty() {
        check(actual != null && !actual.isEmpty(), "Expected: to be non-empty");
    }
}
