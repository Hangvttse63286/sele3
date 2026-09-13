package com.sele3.asserts;

import java.util.function.Consumer;

/**
 * Common state and failure-reporting logic shared by every typed {@code *Expect} matcher
 * (e.g. {@link StringExpect}, {@link NumberExpect}). Callers obtain a built-in matcher from
 * {@link Assert#expect} or {@link SoftAssert#expect}.
 *
 * <p>This class is also the extension point for a custom matcher over your own type, without
 * needing to modify this package: extend it, add whatever {@code toXxx()} methods make sense for
 * your type (each calling {@link #check}), and write your own small entry-point class exposing an
 * {@code expect(YourType actual, String... description)} factory. Wire that factory's hard-assert
 * overload to {@link Assert#fail(AssertionException)} and its soft-assert overload to a
 * {@link SoftAssert} instance's {@link SoftAssert#record(AssertionException)} (e.g.
 * {@code softAssert::record}) so the custom matcher throws/collects exactly like the built-in
 * ones. For example:
 * <pre>{@code
 * public final class PriceExpect extends BaseExpect<Price> {
 *     public PriceExpect(Price actual, String description, Consumer<AssertionException> onFailure) {
 *         super(actual, description, onFailure);
 *     }
 *
 *     public void toBeCheaperThan(Price other) {
 *         check(actual.compareTo(other) < 0, "Expected: to be cheaper than " + format(other));
 *     }
 * }
 *
 * public final class PriceAssertions {
 *     private PriceAssertions() {}
 *
 *     public static PriceExpect expect(Price actual, String... description) {
 *         return new PriceExpect(actual, BaseExpect.describe(description), Assert::fail);
 *     }
 *
 *     public static PriceExpect expect(SoftAssert softAssert, Price actual, String... description) {
 *         return new PriceExpect(actual, BaseExpect.describe(description), softAssert::record);
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the value under assertion
 */
public abstract class BaseExpect<T> {
    protected final T actual;
    private final String description;
    private final Consumer<AssertionException> onFailure;

    protected BaseExpect(T actual, String description, Consumer<AssertionException> onFailure) {
        this.actual = actual;
        this.description = description;
        this.onFailure = onFailure;
    }

    /**
     * Reports a failure via this instance's failure handler unless {@code passed} is {@code true}.
     *
     * @param passed whether the checked condition held
     * @param expectation a human-readable description of what was expected, e.g. {@code "Expected: to equal \"foo\""}
     */
    protected void check(boolean passed, String expectation) {
        if (passed) {
            return;
        }
        StringBuilder message = new StringBuilder();
        if (description != null) {
            message.append(description).append(System.lineSeparator());
        }
        message.append(expectation)
                .append(System.lineSeparator())
                .append("Actual:   ")
                .append(format(actual));
        onFailure.accept(new AssertionException(message.toString()));
    }

    /**
     * Formats a value for inclusion in a failure message, quoting strings so that, e.g., an empty
     * string and {@code null} are visually distinguishable.
     *
     * @param value the value to format
     * @return the formatted value
     */
    protected static String format(Object value) {
        if (value instanceof String s) {
            return "\"" + s + "\"";
        }
        return String.valueOf(value);
    }

    /**
     * Picks the single optional description out of a factory method's {@code String...
     * description} varargs parameter (only the first element is used, if any). A convenience for
     * custom {@code expect} entry points built on this class; the built-in {@code Assert}/
     * {@code SoftAssert} overloads use it via {@link Expects}.
     *
     * @param description zero or one description strings
     * @return the description, or {@code null} if none was given
     */
    public static String describe(String... description) {
        return description.length > 0 ? description[0] : null;
    }
}
