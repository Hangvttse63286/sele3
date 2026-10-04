package com.sele3.reports;

public interface IReportStatus {

    /**
     * Whether this status denotes a failure worth attaching a screenshot to (see
     * {@link IReportFactory#step(IReportStatus, String)}), as opposed to a routine outcome. Of
     * the built-in {@link ReportStatus} constants, {@code FAIL}, {@code WARNING}, and
     * {@code BROKEN} are failures; {@code PASS}, {@code SKIP}, and {@code INFO} are not.
     *
     * @return {@code true} if this status counts as a failure
     */
    boolean isFailureStatus();
}
