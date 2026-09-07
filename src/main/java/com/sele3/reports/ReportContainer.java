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
     * thread. If the {@value ConfigKey#REPORT_TYPE} system property isn't already set, it's set
     * to {@code reportType}'s name as a side effect, so later calls to {@link #getReportType()},
     * {@link #initialize()}, and {@link #flush()} — which have no report type of their own to go
     * on — resolve to whichever type was used first.
     *
     * @param reportType which reporting backend to use; must not be {@code null}
     * @throws IllegalArgumentException if {@code reportType} is {@code null}
     */
    public void initialize(IReportType reportType) {
        if (reportType == null) {
            throw new IllegalArgumentException("No report type specified");
        }
        if (getReportType() == null) {
            System.setProperty(ConfigKey.REPORT_TYPE, reportType.name());
        }
        log.info("Initializing report factory: reportType={}", reportType);
        IReportFactory factory = createReportFactory(reportType);
        threadReportFactory.set(factory);
    }

    /**
     * {@link #initialize(IReportType)} using the type resolved by {@link #getReportType()}; logs
     * a warning and does nothing if the {@value ConfigKey#REPORT_TYPE} system property isn't set.
     */
    public void initialize() {
        IReportType reportType = getReportType();
        if (reportType == null) {
            log.warn("No report type specified via system property '{}'; skipping report factory initialization.", ConfigKey.REPORT_TYPE);
            return;
        }
        initialize(reportType);
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
            .filter(factory -> reportType.equals(factory.getReportType()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported report type: " + reportType + ". Add a META-INF/services/"
                    + IReportFactory.class.getName()
                    + " entry for an IReportFactory implementation whose getReportType() returns it."));
    }

    /**
     * Returns the {@link IReportFactory} bound to the current thread.
     *
     * @return the current thread's {@link IReportFactory}
     * @throws RuntimeException if no report factory has been bound via {@link #initialize(IReportType)}
     */
    public IReportFactory getReportFactory() {
        if (threadReportFactory.get() == null) {
            throw new RuntimeException("No report factory is bound to current thread. You need to initialize the report factory first.");
        }
        return threadReportFactory.get();
    }

    /**
     * Removes the report factory binding for the current thread. {@link ThreadLocal} only ever
     * lets a thread clear its own entry, so this must be called from the same thread that called
     * {@link #initialize}, e.g. from {@link ReportRunner#endTest}.
     */
    public void clear() {
        threadReportFactory.remove();
    }

    /**
     * Builds a fresh {@link IReportFactory} for the type resolved by {@link #getReportType()} and
     * flushes it. Unlike {@link #clear()}, this is safe to call from a thread other than the ones
     * that ran tests (e.g. the suite/launcher thread at the very end of a run), since it never
     * touches the thread-confined {@link ThreadLocal} — it relies only on the
     * {@value ConfigKey#REPORT_TYPE} system property set by whichever thread called
     * {@link #initialize(IReportType)} first.
     *
     * @throws IllegalArgumentException if the {@value ConfigKey#REPORT_TYPE} system property isn't set
     */
    public void flush() {
        createReportFactory(getReportType()).flush();
    }
}
