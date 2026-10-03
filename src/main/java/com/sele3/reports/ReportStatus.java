package com.sele3.reports;

public enum ReportStatus implements IReportStatus {
    PASS,
    FAIL,
    SKIP,
    INFO,
    WARNING,
    BROKEN,
    ;

    @Override
    public boolean isFailureStatus() {
        return this == FAIL || this == WARNING || this == BROKEN;
    }
}
