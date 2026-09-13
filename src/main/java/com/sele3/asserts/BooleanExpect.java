package com.sele3.asserts;

import java.util.function.Consumer;

/**
 * Fluent matchers for a {@code boolean}/{@link Boolean} value, obtained via {@link Assert#expect}
 * or {@link SoftAssert#expect}.
 *
 * <p>Takes a boxed {@link Boolean} rather than a primitive {@code boolean} so that, combined with
 * this API's optional varargs description parameter, overload resolution against the generic
 * {@link ObjectExpect} fallback stays unambiguous (comparing two boxed types is well-defined;
 * comparing a primitive candidate against a generic one under variable-arity invocation is not).
 */
public final class BooleanExpect extends BaseExpect<Boolean> {

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
