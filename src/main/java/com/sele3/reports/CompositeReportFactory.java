package com.sele3.reports;

import java.util.List;

/**
 * {@link IReportFactory} that fans every call out to a fixed set of delegate factories, so
 * callers can report to multiple backends (e.g. Allure and ExtentReports) through a single
 * {@link IReportFactory} without knowing more than one is in play.
 */
public class CompositeReportFactory implements IReportFactory {
    private final List<IReportFactory> factories;

    /**
     * Creates a factory that forwards every call to each of the given factories, in order.
     *
     * @param factories the factories to delegate to
     */
    public CompositeReportFactory(IReportFactory... factories) {
        this.factories = List.of(factories);
    }

    @Override
    public void startTest(String name, String description) {
        factories.forEach(f -> f.startTest(name, description));
    }

    @Override
    public void endTest(ReportStatus status) {
        factories.forEach(f -> f.endTest(status));
    }

    @Override
    public void log(ReportStatus status, String message) {
        factories.forEach(f -> f.log(status, message));
    }

    @Override
    public void step(ReportStatus status, String stepName) {
        factories.forEach(f -> f.step(status, stepName));
    }

    @Override
    public void step(String stepName, Runnable body) {
        // Run body exactly once regardless of how many delegates are registered - only the
        // pass/fail outcome is fanned out, not the step itself (delegates' own step(String,
        // Runnable) overloads are not used here, since that would run body once per delegate).
        try {
            body.run();
            factories.forEach(f -> f.step(ReportStatus.PASS, stepName));
        } catch (Throwable t) {
            factories.forEach(f -> {
                f.step(ReportStatus.FAIL, stepName);
                f.logException(t);
            });
            throw t;
        }
    }

    @Override
    public void logException(Throwable throwable) {
        factories.forEach(f -> f.logException(throwable));
    }

    @Override
    public void attachScreenshot(byte[] screenshot, String name) {
        factories.forEach(f -> f.attachScreenshot(screenshot, name));
    }

    @Override
    public void attachText(String name, String content) {
        factories.forEach(f -> f.attachText(name, content));
    }

    @Override
    public void flush() {
        factories.forEach(IReportFactory::flush);
    }
}
