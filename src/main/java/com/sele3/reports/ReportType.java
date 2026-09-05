package com.sele3.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
    ALLURE,
    EXTENT,
    ALL,
    ;

    /**
     * Resolves a {@link ReportType} from its name, case-insensitively.
     *
     * @param reportType the report type name (e.g. "allure", "Extent", "ALL")
     * @return the matching {@link ReportType}
     * @throws IllegalArgumentException if no matching report type exists
     */
    public static ReportType fromString(String reportType) {
        return ReportType.valueOf(reportType.trim().toUpperCase());
    }
}
