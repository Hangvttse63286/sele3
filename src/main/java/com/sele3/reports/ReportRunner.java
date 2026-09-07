package com.sele3.reports;

import org.openqa.selenium.OutputType;

import com.sele3.drivers.DriverRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportRunner {
    private static final ReportContainer reportContainer = new ReportContainer();

    /**
     * Binds a new {@link IReportFactory} built for {@code reportType} to the current thread and
     * starts a test entry on it.
     *
     * @param reportType which reporting backend to use for this test
     * @param name the test's display name
     * @param description a longer description of what the test verifies, or {@code null} for none
     */
    public static void startTest(IReportType reportType, String name, String description) {
        log.info("Starting test: reportType={}, name={}, description={}", reportType, name, description);
        reportContainer.initialize(reportType);
        getReportFactory().startTest(name, description);
    }

    /**
     * {@link #startTest(IReportType, String, String)} using the report type resolved by
     * {@link ReportContainer#getReportType()} (the {@value com.sele3.configs.ConfigKey#REPORT_TYPE}
     * system property).
     *
     * @param name the test's display name
     * @param description a longer description of what the test verifies, or {@code null} for none
     * @throws IllegalArgumentException if no report type has been configured yet (see {@link #startTest(IReportType, String, String)})
     */
    public static void startTest(String name, String description) {
        IReportType reportType = reportContainer.getReportType();
        startTest(reportType, name, description);
    }

    /**
     * Finalizes the current thread's test entry and clears its report factory binding.
     *
     * @param status the test's final status
     */
    public static void endTest(ReportStatus status) {
        log.info("Ending test: status={}", status);
        getReportFactory().endTest(status);
        reportContainer.clear();
    }

    /**
     * Returns the {@link IReportFactory} bound to the current thread.
     *
     * @return the current thread's {@link IReportFactory}
     * @throws RuntimeException if {@link #startTest(IReportType, String, String)} has not been called on this thread
     */
    public static IReportFactory getReportFactory() {
        return reportContainer.getReportFactory();
    }

    /**
     * @see IReportFactory#log(ReportStatus, String)
     */
    public static void log(ReportStatus status, String message) {
        log.info("[LOG]: {}", message);
        getReportFactory().log(status, message);
    }

    /**
     * @see IReportFactory#step(ReportStatus, String)
     */
    public static void step(ReportStatus status, String stepName) {
        log.info("[STEP]: {} - {}", stepName, status);
        getReportFactory().step(status, stepName);
    }

    /**
     * @see IReportFactory#step(String, Runnable)
     */
    public static void step(String stepName, Runnable body) {
        log.info("[STEP]: {}", stepName);
        getReportFactory().step(stepName, body);
    }

    /**
     * @see IReportFactory#logException(Throwable)
     */
    public static void logException(Throwable throwable) {
        log.error("[EXCEPTION]: {}", throwable.getMessage(), throwable);
        getReportFactory().logException(throwable);
    }

    /**
     * @see IReportFactory#attachScreenshot(byte[], String)
     */
    public static void attachScreenshot(byte[] screenshot, String name) {
        log.info("[ATTACHMENT-SCREENSHOT]: {}", name);
        getReportFactory().attachScreenshot(screenshot, name);
    }

    /**
     * @see IReportFactory#attachText(String, String)
     */
    public static void attachText(String name, String content) {
        log.info("[ATTACHMENT-TEXT]: {}", name);
        getReportFactory().attachText(name, content);
    }

    /**
     * Takes a screenshot and attaches it to the current test.
     *
     * @param name a label for the attachment
     */
    public static void attachScreenshot(String name) {
        attachScreenshot(DriverRunner.takeScreenShot(OutputType.BYTES), name);
    }

    /**
     * @see ReportContainer#flush()
     */
    public static void flush() {
        log.info("Flushing report");
        reportContainer.flush();
    }
}
