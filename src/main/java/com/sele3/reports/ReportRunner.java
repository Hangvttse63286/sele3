package com.sele3.reports;

import java.util.Optional;

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
        getReportFactory().orElseThrow().startTest(name, description);
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
        log.info("Ending test: status={}", status);
        getReportFactory().ifPresent(factory -> {
            factory.endTest(status);
        });
        reportContainer.clear();
    }

    /**
     * Returns the {@link IReportFactory} bound to the current thread.
     *
     * @return the current thread's {@link IReportFactory}, or empty if reporting is disabled for
     *         this run (see {@link #startTest(IReportType, String, String)})
     */
    public static Optional<IReportFactory> getReportFactory() {
        return Optional.ofNullable(reportContainer.getReportFactory());
    }

    /**
     * @see IReportFactory#log(IReportStatus, String)
     */
    public static void log(IReportStatus status, String message) {
        log.info("[LOG]: {}", message);
        getReportFactory().ifPresent(factory -> {
            factory.log(status, message);
        });
    }

    /**
     * @see IReportFactory#step(IReportStatus, String)
     */
    public static void step(IReportStatus status, String stepName) {
        log.info("[STEP]: {} - {}", stepName, status);
        getReportFactory().ifPresent(factory -> {
            factory.step(status, stepName);
        });
    }

    /**
     * @see IReportFactory#step(String, Runnable)
     */
    public static void step(String stepName, Runnable body) {
        log.info("[STEP]: {}", stepName);
        Optional<IReportFactory> factory = getReportFactory();
        if (factory.isEmpty()) {
            body.run();
            return;
        }
        factory.get().step(stepName, body);
    }

    /**
     * @see IReportFactory#logException(Throwable)
     */
    public static void logException(Throwable throwable) {
        log.error("[EXCEPTION]: {}", throwable.getMessage(), throwable);
        getReportFactory().ifPresent(factory -> {
            factory.logException(throwable);
        });
    }

    /**
     * @see IReportFactory#attachScreenshot(String, String)
     */
    public static void attachScreenshot(String screenshotBase64, String name) {
        log.info("[ATTACHMENT-SCREENSHOT]: {}", name);
        getReportFactory().ifPresent(factory -> {
            factory.attachScreenshot(screenshotBase64, name);
        });
    }

    /**
     * @see IReportFactory#attachText(String, String)
     */
    public static void attachText(String name, String content) {
        log.info("[ATTACHMENT-TEXT]: {}", name);
        getReportFactory().ifPresent(factory -> {
            factory.attachText(name, content);
        });
    }

    /**
     * Takes a screenshot and attaches it to the current test. Skips taking the screenshot
     * entirely if reporting is disabled, since there would be nowhere to attach it.
     *
     * @param name a label for the attachment
     */
    public static void attachScreenshot(String name) {
        if (getReportFactory().isPresent()) {
            attachScreenshot(DriverRunner.takeScreenShot(OutputType.BASE64), name);
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
