package com.sele3.asserts;

import java.util.function.Consumer;

import com.sele3.reports.ReportRunner;
import com.sele3.reports.ReportStatus;

/**
 * Common state and failure-reporting logic shared by every typed {@code *Expect} matcher
 * (e.g. {@link StringExpect}, {@link NumberExpect}). Callers obtain a built-in matcher from
 * {@link Assert#expect} or {@link SoftAssert#expect}.
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
     * Reports this check as a report step — {@link ReportStatus#PASS} or {@link ReportStatus#FAIL}
     * — and, unless {@code passed} is {@code true}, also reports a failure via this instance's
     * failure handler.
     *
     * @param passed whether the checked condition held
     * @param expectation a human-readable description of what was expected, e.g. {@code "Expected: to equal \"foo\""}
     */
    protected void check(boolean passed, String expectation) {
        String message = buildMessage(expectation);
        ReportRunner.step(passed ? ReportStatus.PASS : ReportStatus.FAIL, message);
        if (passed) {
            return;
        }
        onFailure.accept(new AssertionException(message));
    }

    private String buildMessage(String expectation) {
        StringBuilder message = new StringBuilder();
        if (description != null) {
            message.append(description).append(System.lineSeparator());
        }
        message.append(expectation);
        if (includeActualInMessage()) {
            message.append(System.lineSeparator())
                    .append("Actual:   ")
                    .append(format(actual));
        }
        return message.toString();
    }

    /**
     * Whether {@link #buildMessage} should append an "Actual: ..." line showing {@link #actual}.
     * True by default; override to suppress it when {@code actual} itself isn't meaningful to
     * show, e.g. {@link ElementExpect}, where {@code actual} is the element wrapper/locator
     * rather than the live DOM state that was actually checked.
     *
     * @return whether to include the Actual line
     */
    protected boolean includeActualInMessage() {
        return true;
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
     * {@code SoftAssert} overloads use it directly when constructing their typed matchers.
     *
     * @param description zero or one description strings
     * @return the description, or {@code null} if none was given
     */
    public static String describe(String... description) {
        return description.length > 0 ? description[0] : null;
    }
}
