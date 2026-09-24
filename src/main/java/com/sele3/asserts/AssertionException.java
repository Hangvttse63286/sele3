package com.sele3.asserts;

/**
 * Thrown when a single assertion fails. Raised immediately by {@link Assert}; collected instead
 * of thrown by {@link SoftAssert}, which reports every collected instance together via
 * {@link SoftAssertionException} when {@link SoftAssert#assertAll()} is called.
 *
 * <p>Deliberately independent of {@code org.testng.Assert}/JUnit's assertion machinery: this is a
 * plain {@link RuntimeException} carrying only a formatted failure message.
 */
public class AssertionException extends RuntimeException {

    /**
     * Creates an assertion failure with the given message.
     *
     * @param message the formatted failure message
     */
    public AssertionException(String message) {
        super(message);
    }
}
