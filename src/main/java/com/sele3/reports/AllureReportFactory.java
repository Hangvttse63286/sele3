package com.sele3.reports;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.UUID;

import org.openqa.selenium.OutputType;

import com.sele3.drivers.DriverRunner;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.util.ResultsUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllureReportFactory implements IReportFactory {

    @Override
    public IReportType getReportType() {
        return ReportType.ALLURE;
    }

    @Override
    public void startTest(String name, String description) {
        log.debug("Allure test lifecycle is managed by the listener; name={}", name);
        if (description != null && !description.isBlank()) {
            Allure.description(description);
        }
    }

    @Override
    public void endTest(IReportStatus status) {
        log.debug("Allure test lifecycle is managed by the listener; final status={}", status);
    }

    @Override
    public void log(IReportStatus status, String message) {
        Allure.step(message, toAllureStatus(status));
    }

    @Override
    public void step(IReportStatus status, String stepName) {
        // Not Allure.step(name, status): a screenshot needs to attach to the step while it's
        // still open, so the step is driven manually instead of via that closed one-liner.
        String uuid = UUID.randomUUID().toString();
        Allure.getLifecycle().startStep(uuid, new StepResult().setName(stepName).setStatus(toAllureStatus(status)));
        if (status.isFailureStatus()) {
            attachScreenshot(DriverRunner.takeScreenShot(OutputType.BASE64), "Screenshot on failure");
        }
        Allure.getLifecycle().stopStep(uuid);
    }

    @Override
    public void step(String stepName, Runnable body) {
        // Not Allure.step(name, body::run): we need the exception in hand, before the step
        // closes, to attach a screenshot to it when it's broken (not a plain assertion failure).
        String uuid = UUID.randomUUID().toString();
        Allure.getLifecycle().startStep(uuid, new StepResult().setName(stepName));
        try {
            body.run();
            Allure.getLifecycle().updateStep(uuid, step -> step.setStatus(Status.PASSED));
        } catch (Throwable t) {
            Status status = ResultsUtils.getStatus(t).orElse(Status.BROKEN);
            Allure.getLifecycle().updateStep(uuid, step -> step
                    .setStatus(status)
                    .setStatusDetails(ResultsUtils.getStatusDetails(t).orElse(null)));
            if (status == Status.BROKEN) {
                attachScreenshot(DriverRunner.takeScreenShot(OutputType.BASE64), "Screenshot on failure");
            }
            throw t;
        } finally {
            Allure.getLifecycle().stopStep(uuid);
        }
    }

    @Override
    public void logException(Throwable throwable) {
        Allure.step(throwable.getMessage() != null ? throwable.getMessage() : throwable.toString(), Status.FAILED);
        Allure.addAttachment("Exception", "text/plain", stackTraceToString(throwable), ".txt");
    }

    @Override
    public void attachScreenshot(String screenshotBase64, String name) {
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(Base64.getDecoder().decode(screenshotBase64)), ".png");
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
    private static Status toAllureStatus(IReportStatus status) {
        return switch (status) {
            case ReportStatus.PASS, ReportStatus.INFO -> Status.PASSED;
            case ReportStatus.FAIL -> Status.FAILED;
            case ReportStatus.SKIP -> Status.SKIPPED;
            case ReportStatus.WARNING, ReportStatus.BROKEN -> Status.BROKEN;
            default -> throw new IllegalArgumentException("Unknown ReportStatus: " + status);
        };
    }

    private static String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
