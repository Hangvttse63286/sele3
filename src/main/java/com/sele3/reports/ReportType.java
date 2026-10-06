package com.sele3.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType implements IReportType {
    ALLURE,
    EXTENT,
    ;
}
