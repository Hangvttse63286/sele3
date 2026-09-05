package com.sele3.reports;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllureReportFactory implements IReportFactory {

    @Override
    public void startTest(String name, String description) {
        log.debug("Allure test lifecycle is managed by the listener; name={}", name);
        if (description != null && !description.isBlank()) {
            Allure.description(description);
        }
    }

    @Override
    public void endTest(ReportStatus status) {
        log.debug("Allure test lifecycle is managed by the listener; final status={}", status);
    }

    @Override
    public void log(ReportStatus status, String message) {
        Allure.step(message, toAllureStatus(status));
    }

    @Override
    public void step(ReportStatus status, String stepName) {
        Allure.step(stepName, toAllureStatus(status));
    }

    @Override
    public void step(String stepName, Runnable body) {
        Allure.step(stepName, body::run);
    }

    @Override
    public void logException(Throwable throwable) {
        Allure.step(throwable.getMessage() != null ? throwable.getMessage() : throwable.toString(), Status.FAILED);
        Allure.addAttachment("Exception", "text/plain", stackTraceToString(throwable), ".txt");
    }

    @Override
    public void attachScreenshot(byte[] screenshot, String name) {
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
    }

    @Override
    public void attachText(String name, String content) {
        Allure.addAttachment(name, content);
    }

    @Override
    public void flush() {
        log.debug("Allure writes each result to disk as it happens; nothing to flush");
    }

    /**
     * Maps a {@link ReportStatus} onto Allure's {@link Status}. Allure has no direct equivalent
     * of {@code INFO}/{@code WARNING}, so {@code INFO} maps to {@code PASSED} and {@code WARNING}
     * maps to {@code BROKEN} (closest match: worth attention but not an assertion failure).
     */
    private static Status toAllureStatus(ReportStatus status) {
        return switch (status) {
            case PASS, INFO -> Status.PASSED;
            case FAIL -> Status.FAILED;
            case SKIP -> Status.SKIPPED;
            case WARNING -> Status.BROKEN;
        };
    }

    private static String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
