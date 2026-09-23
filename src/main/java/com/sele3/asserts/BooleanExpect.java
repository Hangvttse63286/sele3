package com.sele3.asserts;

import java.util.function.Consumer;

/**
 * Fluent matchers for a {@code boolean}/{@link Boolean} value, obtained via {@link Assert#expect}
 * or {@link SoftAssert#expect}.
 */
public final class BooleanExpect extends ObjectExpect<Boolean> {

    BooleanExpect(Boolean actual, String description, Consumer<AssertionException> onFailure) {
        super(actual, description, onFailure);
    }

    /**
     * Asserts that the actual value is {@code true}.
     */
    public void toBeTrue() {
        check(actual != null && actual, "Expected: to be true");
    }

    /**
     * Asserts that the actual value is {@code false}.
     */
    public void toBeFalse() {
        check(actual != null && !actual, "Expected: to be false");
    }
}
