package com.sele3.asserts;

import org.assertj.core.api.Assertions;

import com.sele3.elements.BaseElement;

/**
 * Entry point for hard (fail-fast) assertions: AssertJ's {@link Assertions} plus
 * {@link #assertThat(BaseElement)} for elements. A failure throws immediately, stopping the
 * current test. For assertions that collect failures instead, see {@link SoftAssert}.
 */
public final class Assert extends Assertions {

    private Assert() {
    }

    /**
     * Starts an assertion on a {@link BaseElement}. Each check polls the element up to the
     * current driver's configured timeout before failing; see {@link ElementAssert}.
     *
     * @param actual the element under assertion
     * @return assertions for {@code actual}
     */
    public static ElementAssert assertThat(BaseElement actual) {
        return new ElementAssert(actual);
    }
}
