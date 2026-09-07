package com.sele3.reports;

import java.util.List;
import java.util.ServiceLoader;

public interface IReportType {

    /**
     * The stable name used to select this type from configuration (e.g. a system property),
     * matched case-insensitively by {@link #fromString}. Enum implementations get this for free
     * from {@link Enum#name()}.
     *
     * @return this type's name
     */
    String name();

    /**
     * Resolves an {@link IReportType} by name, discovering candidates the same way
     * {@link ReportContainer} discovers factories: via every {@link IReportFactory} registered in
     * a {@code META-INF/services/com.sele3.reports.IReportFactory} file on the classpath. A new
     * type added purely by registering its own {@link IReportFactory} therefore becomes
     * resolvable here too, with no change to this class.
     *
     * @param name the type's name (e.g. "allure", "REPORT_PORTAL"), matched case-insensitively
     * @return the matching {@link IReportType}
     * @throws IllegalArgumentException if no registered report type matches {@code name}
     */
    static IReportType fromString(String name) {
        return ServiceLoader.load(IReportFactory.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(IReportFactory::getReportType)
            .filter(type -> type.name().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown report type: " + name + ". Add a META-INF/services/"
                    + IReportFactory.class.getName()
                    + " entry for an IReportFactory implementation whose getReportType().name() matches it."));
    }

    /**
     * Lists every registered {@link IReportType} — predefined ({@link ReportType#ALLURE},
     * {@link ReportType#EXTENT}) and any added later — by the same {@link IReportFactory}
     * discovery {@link #fromString} uses, so a type registered purely via its own
     * {@link IReportFactory} appears here too with no change to this class.
     *
     * @return every registered report type, in no particular order, with no duplicates
     */
    static List<IReportType> values() {
        return ServiceLoader.load(IReportFactory.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(IReportFactory::getReportType)
            .distinct()
            .toList();
    }
}
