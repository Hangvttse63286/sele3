package com.sele3.reports;

import org.openqa.selenium.OutputType;

import com.sele3.drivers.DriverRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportRunner {
    private static final ReportContainer reportContainer = new ReportContainer();

    /**
     * Binds a new {@link IReportFactory} built for {@code reportType} to the current thread and
     * starts a test entry on it, or — if {@code reportType} is {@code null} — disables reporting
     * for this test entirely: every other method below becomes a no-op (logging that fact once,
     * here) instead of throwing, so callers (e.g. a test listener) can call the full lifecycle
     * unconditionally regardless of whether a report type is configured.
     *
     * @param reportType which reporting backend to use for this test, or {@code null} to run without generating a report
     * @param name the test's display name
     * @param description a longer description of what the test verifies, or {@code null} for none
     */
    public static void startTest(IReportType reportType, String name, String description) {
        reportContainer.initialize(reportType);
        if (reportType == null) {
            log.info("No report type configured; not generating a report for test '{}'.", name);
            return;
        }
        log.info("Starting test: reportType={}, name={}, description={}", reportType, name, description);
        getReportFactory().startTest(name, description);
    }

    /**
     * {@link #startTest(IReportType, String, String)} using the report type resolved by
     * {@link ReportContainer#getReportType()} (the {@value com.sele3.configs.ConfigKey#REPORT_TYPE}
     * system property), or no report type at all if that property isn't set.
     *
     * @param name the test's display name
     * @param description a longer description of what the test verifies, or {@code null} for none
     */
    public static void startTest(String name, String description) {
        startTest(reportContainer.getReportType(), name, description);
    }

    /**
     * Finalizes the current thread's test entry (if reporting is enabled) and clears its report
     * factory binding.
     *
     * @param status the test's final status
     */
    public static void endTest(IReportStatus status) {
        IReportFactory factory = getReportFactory();
        if (factory != null) {
            log.info("Ending test: status={}", status);
            factory.endTest(status);
        }
        reportContainer.clear();
    }

    /**
     * Returns the {@link IReportFactory} bound to the current thread.
     *
     * @return the current thread's {@link IReportFactory}, or {@code null} if reporting is
     *         disabled for this run (see {@link #startTest(IReportType, String, String)})
     */
    public static IReportFactory getReportFactory() {
        return reportContainer.getReportFactory();
    }

    /**
     * @see IReportFactory#log(IReportStatus, String)
     */
    public static void log(IReportStatus status, String message) {
        IReportFactory factory = getReportFactory();
        if (factory != null) {
            log.info("[LOG]: {}", message);
            factory.log(status, message);
        }
    }

    /**
     * @see IReportFactory#step(IReportStatus, String)
     */
    public static void step(IReportStatus status, String stepName) {
        IReportFactory factory = getReportFactory();
        if (factory != null) {
            log.info("[STEP]: {} - {}", stepName, status);
            factory.step(status, stepName);
        }
    }

    /**
     * @see IReportFactory#step(String, Runnable)
     */
    public static void step(String stepName, Runnable body) {
        IReportFactory factory = getReportFactory();
        if (factory == null) {
            body.run();
            return;
        }
        log.info("[STEP]: {}", stepName);
        factory.step(stepName, body);
    }

    /**
     * @see IReportFactory#logException(Throwable)
     */
    public static void logException(Throwable throwable) {
        IReportFactory factory = getReportFactory();
        if (factory != null) {
            log.error("[EXCEPTION]: {}", throwable.getMessage(), throwable);
            factory.logException(throwable);
        }
    }

    /**
     * @see IReportFactory#attachScreenshot(byte[], String)
     */
    public static void attachScreenshot(byte[] screenshot, String name) {
        IReportFactory factory = getReportFactory();
        if (factory != null) {
            log.info("[ATTACHMENT-SCREENSHOT]: {}", name);
            factory.attachScreenshot(screenshot, name);
        }
    }

    /**
     * @see IReportFactory#attachText(String, String)
     */
    public static void attachText(String name, String content) {
        IReportFactory factory = getReportFactory();
        if (factory != null) {
            log.info("[ATTACHMENT-TEXT]: {}", name);
            factory.attachText(name, content);
        }
    }

    /**
     * Takes a screenshot and attaches it to the current test. Skips taking the screenshot
     * entirely if reporting is disabled, since there would be nowhere to attach it.
     *
     * @param name a label for the attachment
     */
    public static void attachScreenshot(String name) {
        if (getReportFactory() != null) {
            attachScreenshot(DriverRunner.takeScreenShot(OutputType.BYTES), name);
        }
    }

    /**
     * @see ReportContainer#flush()
     */
    public static void flush() {
        log.info("Flushing report");
        reportContainer.flush();
    }
}
