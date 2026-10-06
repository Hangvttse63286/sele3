package com.sele3.reports;

public interface IReportFactory {

    /**
     * Returns the {@link IReportType} that this factory produces.
     *
     * @return the report type
     */
    IReportType getReportType();

    /**
     * Starts a new test entry in the report, becoming the target of subsequent
     * {@link #log}/{@link #logException}/{@link #attachScreenshot}/{@link #attachText}/
     * {@link #step} calls until {@link #endTest} is called.
     *
     * @param name the test's display name
     * @param description a longer description of what the test verifies, or {@code null} for none
     */
    void startTest(String name, String description);

    /**
     * Finalizes the current test entry with the given overall status.
     *
     * @param status the test's final status
     */
    void endTest(IReportStatus status);

    /**
     * Finalizes the current test entry, recording why it failed or was skipped. By default logs
     * {@code error} first: a skip reason as a {@link ReportStatus#SKIP} message, anything else via
     * {@link #logException}. A backend whose test-runner integration already records it (e.g.
     * Allure) overrides this to avoid a duplicate.
     *
     * @param status the test's final status
     * @param error the error that failed the test or the reason it was skipped, or {@code null}
     */
    default void endTest(IReportStatus status, Throwable error) {
        if (error != null) {
            if (status == ReportStatus.SKIP) {
                log(status, String.valueOf(error.getMessage()));
            } else {
                logException(error);
            }
        }
        endTest(status);
    }

    /**
     * Logs a single message against the current test at the given status.
     *
     * @param status the log level to record the message at
     * @param message the message to record
     */
    void log(IReportStatus status, String message);

    /**
     * Records a named step against the current test at the given status, grouping any log
     * lines that logically belong to that step. A screenshot is attached to the step when
     * {@link IReportStatus#isFailureStatus() status.isFailureStatus()} is {@code true}.
     *
     * @param status the step's outcome
     * @param stepName a short description of the step performed
     */
    void step(IReportStatus status, String stepName);

    /**
     * Runs {@code body} as a named step against the current test, recording it as
     * {@link ReportStatus#PASS} if it returns normally or a failure status with a screenshot if it
     * throws, then rethrows so a failing step still fails the test.
     *
     * @param stepName a short description of the step performed
     * @param body the code to run as this step
     */
    void step(String stepName, Runnable body);

    /**
     * Logs an exception against the current test, including its stack trace.
     *
     * @param throwable the exception to record
     */
    void logException(Throwable throwable);

    /**
     * Attaches a screenshot to the current test.
     *
     * @param screenshotBase64 the screenshot as a base64-encoded PNG (Selenium's native
     *        {@code TakesScreenshot} wire format, e.g. via {@code OutputType.BASE64} — no
     *        decode/encode round-trip needed to obtain it)
     * @param name a label for the attachment
     */
    void attachScreenshot(String screenshotBase64, String name);

    /**
     * Attaches arbitrary text content (e.g. page source, a JSON payload, driver logs) to the
     * current test.
     *
     * @param name a label for the attachment
     * @param content the text content to attach
     */
    void attachText(String name, String content);

    /**
     * Writes/publishes the accumulated report to its output location. Implementations for which
     * results are written incrementally (e.g. Allure's file-per-result model) may treat this as
     * a no-op.
     */
    void flush();
}
