package com.sele3.reports;

public interface IReportFactory {

    /**
     * Starts a new test entry in the report, becoming the target of subsequent
     * {@link #log}/{@link #logException}/{@link #attachScreenshot}/{@link #attachText}/
     * {@link #step} calls until {@link #endTest()} is called.
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
    void endTest(ReportStatus status);

    /**
     * Logs a single message against the current test at the given status.
     *
     * @param status the log level to record the message at
     * @param message the message to record
     */
    void log(ReportStatus status, String message);

    /**
     * Records a named step against the current test at the given status, grouping any log
     * lines that logically belong to that step.
     *
     * @param status the step's outcome
     * @param stepName a short description of the step performed
     */
    void step(ReportStatus status, String stepName);

    /**
     * Runs {@code body} as a named step against the current test, recording it as
     * {@link ReportStatus#PASS} if {@code body} returns normally, or as
     * {@link ReportStatus#FAIL} (with the exception recorded) if it throws. The exception is
     * always rethrown after being recorded, so a failing step still fails the calling test.
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
     * @param screenshot the screenshot bytes (e.g. PNG, as returned by {@code TakesScreenshot})
     * @param name a label for the attachment
     */
    void attachScreenshot(byte[] screenshot, String name);

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
