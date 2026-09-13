package com.sele3.asserts;

import java.util.List;

/**
 * Thrown by {@link SoftAssert#assertAll()} when one or more soft assertions failed. Aggregates
 * every {@link AssertionException} collected on the calling thread since the last
 * {@link SoftAssert#assertAll()}/{@link SoftAssert#reset()}, formatting them into a single
 * numbered report.
 */
public class SoftAssertionException extends RuntimeException {
    private final transient List<AssertionException> failures;

    /**
     * Creates an aggregate failure from the given collected soft-assertion failures.
     *
     * @param failures the failures to report, in the order they were recorded; must not be empty
     */
    public SoftAssertionException(List<AssertionException> failures) {
        super(buildMessage(failures));
        this.failures = List.copyOf(failures);
    }

    /**
     * Returns the individual failures that make up this aggregate, in the order they were recorded.
     *
     * @return the collected failures
     */
    public List<AssertionException> getFailures() {
        return failures;
    }

    private static String buildMessage(List<AssertionException> failures) {
        StringBuilder message = new StringBuilder()
                .append(failures.size())
                .append(failures.size() == 1 ? " assertion failed:" : " assertions failed:");
        for (int i = 0; i < failures.size(); i++) {
            message.append(System.lineSeparator())
                    .append(i + 1)
                    .append(") ")
                    .append(failures.get(i).getMessage());
        }
        return message.toString();
    }
}
