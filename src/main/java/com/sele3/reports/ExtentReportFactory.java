package com.sele3.reports;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtentReportFactory implements IReportFactory {
    private static final String OUTPUT_PATH = "target/reports/extent-reports/index.html";
    private static final ExtentReports extent = createExtentReports();

    private ExtentTest currentTest;

    @Override
    public void startTest(String name, String description) {
        this.currentTest = extent.createTest(name, description);
    }

    @Override
    public void endTest(ReportStatus status) {
        requireCurrentTest().log(toExtentStatus(status), "Test finished with status: " + status);
        this.currentTest = null;
    }

    @Override
    public void log(ReportStatus status, String message) {
        requireCurrentTest().log(toExtentStatus(status), message);
    }

    @Override
    public void step(ReportStatus status, String stepName) {
        requireCurrentTest().createNode(stepName).log(toExtentStatus(status), stepName);
    }

    @Override
    public void step(String stepName, Runnable body) {
        ExtentTest node = requireCurrentTest().createNode(stepName);
        try {
            body.run();
            node.pass(stepName);
        } catch (Throwable t) {
            node.fail(stepName + " failed with exception: " + t.getMessage());
            throw t;
        }
    }

    @Override
    public void logException(Throwable throwable) {
        requireCurrentTest().log(Status.FAIL, throwable);
    }

    @Override
    public void attachScreenshot(byte[] screenshot, String name) {
        requireCurrentTest().addScreenCaptureFromBase64String(Base64.getEncoder().encodeToString(screenshot), name);
    }

    @Override
    public void attachText(String name, String content) {
        requireCurrentTest().info(name);
        requireCurrentTest().info(MarkupHelper.createCodeBlock(content));
    }

    @Override
    public void flush() {
        extent.flush();
    }

    private ExtentTest requireCurrentTest() {
        if (currentTest == null) {
            throw new RuntimeException("No Extent test has been started. Call startTest() first.");
        }
        return currentTest;
    }

    /**
     * {@link ReportStatus} and ExtentReports' {@link Status} share the same constant names
     * ({@code PASS}/{@code FAIL}/{@code SKIP}/{@code INFO}/{@code WARNING}), so this is a
     * one-to-one mapping rather than an approximation.
     */
    private static Status toExtentStatus(ReportStatus status) {
        return switch (status) {
            case PASS -> Status.PASS;
            case FAIL -> Status.FAIL;
            case SKIP -> Status.SKIP;
            case INFO -> Status.INFO;
            case WARNING -> Status.WARNING;
        };
    }

    private static ExtentReports createExtentReports() {
        try {
            Files.createDirectories(Path.of(OUTPUT_PATH).getParent());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create Extent report output directory", e);
        }

        ExtentReports extentReports = new ExtentReports();
        extentReports.attachReporter(new ExtentSparkReporter(OUTPUT_PATH));
        return extentReports;
    }
}
