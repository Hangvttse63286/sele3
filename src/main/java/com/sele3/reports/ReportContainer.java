package com.sele3.reports;

import java.util.ServiceLoader;

import com.sele3.configs.ConfigKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportContainer {
    private final ThreadLocal<IReportFactory> threadReportFactory = new ThreadLocal<>();

    /**
     * Resolves the {@link IReportType} currently configured via the
     * {@value ConfigKey#REPORT_TYPE} system property.
     *
     * @return the configured report type, or {@code null} if the property isn't set
     * @throws IllegalArgumentException if the property is set to a name that isn't a registered {@link IReportType}
     */
    public IReportType getReportType() {
        String reportTypeName = System.getProperty(ConfigKey.REPORT_TYPE, "");
        return !reportTypeName.isBlank() ? IReportType.fromString(reportTypeName) : null;
    }

    /**
     * Creates an {@link IReportFactory} for {@code reportType} and binds it to the current
     * thread, or clears any existing binding if {@code reportType} is {@code null} (no reporting
     * for this run — see {@link #getReportFactory()}). If the {@value ConfigKey#REPORT_TYPE}
     * system property isn't already set, it's set to {@code reportType}'s name as a side effect,
     * so later calls to {@link #getReportType()} and {@link #initialize()} — which have no report
     * type of their own to go on — resolve to whichever type was used first.
     *
     * @param reportType which reporting backend to use, or {@code null} to disable reporting
     */
    public void initialize(IReportType reportType) {
        System.setProperty(ConfigKey.REPORT_TYPE, reportType == null ? "" : reportType.name());
        if (reportType == null) {
            threadReportFactory.remove();
            return;
        }
        log.info("Initializing report factory: reportType={}", reportType);
        threadReportFactory.set(createReportFactory(reportType));
    }

    /**
     * {@link #initialize(IReportType)} using the type resolved by {@link #getReportType()}.
     */
    public void initialize() {
        initialize(getReportType());
    }

    /**
     * Builds the {@link IReportFactory} whose {@link IReportFactory#getReportType()} matches
     * {@code reportType}, discovered via {@link ServiceLoader} from every
     * {@code META-INF/services/com.sele3.reports.IReportFactory} entry on the classpath. Adding
     * support for a new {@link IReportType} is therefore purely additive from outside this
     * package: implement {@link IReportFactory} (with a public no-arg constructor) and list it in
     * that services file — nothing here needs to change.
     *
     * @throws IllegalArgumentException if no registered {@link IReportFactory} matches {@code reportType}
     */
    private static IReportFactory createReportFactory(IReportType reportType) {
        return ServiceLoader.load(IReportFactory.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(factory -> reportType.name().equalsIgnoreCase(factory.getReportType().name()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported report type: " + reportType + ". Add a META-INF/services/"
                    + IReportFactory.class.getName()
                    + " entry for an IReportFactory implementation whose getReportType() returns it."));
    }

    /**
     * Returns the {@link IReportFactory} bound to the current thread.
     *
     * @return the current thread's {@link IReportFactory}, or {@code null} if
     *         {@link #initialize(IReportType)} was never called on this thread, or was last
     *         called with {@code null} (reporting disabled for this run)
     */
    public IReportFactory getReportFactory() {
        return threadReportFactory.get();
    }

    /**
     * Flushes and removes the report factory binding for the current thread, or does nothing if
     * reporting is disabled for this run (no factory bound). {@link ThreadLocal} only ever lets a
     * thread clear its own entry, so this must be called from the same thread that called
     * {@link #initialize}, e.g. from {@link ReportRunner#endTest} — which is also why flushing
     * happens here per-test rather than once at the end of a suite: each test's own worker thread
     * flushes its own factory, so no cross-thread access to another thread's binding is needed.
     */
    public void clear() {
        IReportFactory factory = getReportFactory();
        if (factory != null) {
            factory.flush();
        }
        threadReportFactory.remove();
    }
}
