package com.sele3.asserts;

import java.util.Collection;
import java.util.function.Consumer;

import com.sele3.elements.BaseElement;

/**
 * Builds the typed {@code *Expect} matcher for a value, sharing the overload set between
 * {@link Assert} (which fails immediately) and {@link SoftAssert} (which records instead of
 * failing immediately). Each factory method optionally accepts a single description string
 * (via varargs, so it can be omitted) that is prefixed to any failure message produced from the
 * returned matcher.
 */
final class Expects {

    private Expects() {
    }

    static <T> ObjectExpect<T> object(T actual, Consumer<AssertionException> onFailure, String... description) {
        return new ObjectExpect<>(actual, BaseExpect.describe(description), onFailure);
    }

    static BooleanExpect bool(Boolean actual, Consumer<AssertionException> onFailure, String... description) {
        return new BooleanExpect(actual, BaseExpect.describe(description), onFailure);
    }

    static StringExpect string(String actual, Consumer<AssertionException> onFailure, String... description) {
        return new StringExpect(actual, BaseExpect.describe(description), onFailure);
    }

    static <T extends Number & Comparable<T>> NumberExpect<T> number(T actual, Consumer<AssertionException> onFailure, String... description) {
        return new NumberExpect<>(actual, BaseExpect.describe(description), onFailure);
    }

    static <E> CollectionExpect<E> collection(Collection<E> actual, Consumer<AssertionException> onFailure, String... description) {
        return new CollectionExpect<>(actual, BaseExpect.describe(description), onFailure);
    }

    static ElementExpect element(BaseElement actual, Consumer<AssertionException> onFailure, String... description) {
        return new ElementExpect(actual, BaseExpect.describe(description), onFailure);
    }
}
