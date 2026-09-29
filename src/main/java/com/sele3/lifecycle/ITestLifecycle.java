package com.sele3.lifecycle;

import com.sele3.asserts.SoftAssert;
import com.sele3.reports.ReportRunner;
import com.sele3.reports.ReportStatus;

/**
 * Framework-agnostic test lifecycle: starts and ends the report entry and checks pending soft
 * assertions. Implement it in your test runner's listener/extension and call these methods from
 * its per-test hooks; e.g. for TestNG, {@link #startTest} from
 * {@code IInvokedMethodListener.beforeInvocation} and {@link #endTest} from {@code afterInvocation}.
 *
 * <p>{@link #endTest} must be called from a hook that can still change the test's result (not
 * after the runner has already recorded it as passed), so that soft-assertion failures fail it.
 */
public interface ITestLifecycle {

    /**
     * Starts the report entry for a test.
     *
     * @param name the test name
     * @param description the test description, or {@code null}
     */
    default void startTest(String name, String description) {
        ReportRunner.startTest(name, description);
    }

    /**
     * Checks pending soft assertions, then ends the report entry as PASS or FAIL.
     *
     * @param testError what the test threw, or {@code null} if it returned normally
     * @return the error the test should fail with ({@code testError}, with any soft-assertion
     *         failures added as suppressed, or the soft-assertion failures alone), or {@code null}
     *         if it passed
     */
    default Throwable endTest(Throwable testError) {
        Throwable error = testError;
        try {
            SoftAssert.softly.assertAll();
        } catch (AssertionError softFailures) {
            if (error == null) {
                error = softFailures;
            } else {
                error.addSuppressed(softFailures);
            }
        }
        if (error != null) {
            ReportRunner.logException(error);
        }
        ReportRunner.endTest(error == null ? ReportStatus.PASS : ReportStatus.FAIL);
        return error;
    }

    /**
     * Ends the report entry as SKIP, discarding any pending soft-assertion failures so they don't
     * leak into the next test.
     *
     * @param reason why the test was skipped, or {@code null}
     */
    default void skipTest(Throwable reason) {
        try {
            SoftAssert.softly.assertAll();
        } catch (AssertionError ignored) {
            // Already reported as FAIL steps when collected; a skipped test doesn't fail on them.
        }
        if (reason != null) {
            ReportRunner.log(ReportStatus.SKIP, String.valueOf(reason.getMessage()));
        }
        ReportRunner.endTest(ReportStatus.SKIP);
    }
}
