package com.sele3.reports;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportContainer {
    private final ThreadLocal<IReportFactory> threadReportFactory = new ThreadLocal<>();

    /**
     * Creates an {@link IReportFactory} for the given {@link ReportType} and binds it to the
     * current thread.
     *
     * @param reportType which reporting backend(s) to use
     */
    public void initialize(ReportType reportType) {
        log.info("Initializing report factory: reportType={}", reportType);
        threadReportFactory.set(createReportFactory(reportType));
    }

    /**
     * Builds the {@link IReportFactory} for a single {@link ReportType}. Package-visible so
     * {@link ReportRunner#flush()} can build the same factory shape to flush, without needing a
     * per-thread binding of its own.
     */
    static IReportFactory createReportFactory(ReportType reportType) {
        return switch (reportType) {
            case ALLURE -> new AllureReportFactory();
            case EXTENT -> new ExtentReportFactory();
            case ALL -> new CompositeReportFactory(new AllureReportFactory(), new ExtentReportFactory());
        };
    }

    /**
     * Returns the {@link IReportFactory} bound to the current thread.
     *
     * @return the current thread's {@link IReportFactory}
     * @throws RuntimeException if no report factory has been bound via {@link #initialize(ReportType)}
     */
    public IReportFactory getReportFactory() {
        if (threadReportFactory.get() == null) {
            throw new RuntimeException("No report factory is bound to current thread. You need to initialize the report factory first.");
        }
        return threadReportFactory.get();
    }

    /**
     * Removes the report factory binding for the current thread.
     */
    public void clear() {
        threadReportFactory.remove();
    }
}
