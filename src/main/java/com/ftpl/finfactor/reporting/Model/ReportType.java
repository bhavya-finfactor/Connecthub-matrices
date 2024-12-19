package com.ftpl.finfactor.reporting.Model;

public enum ReportType {
    CONNECTHUB_DFS,
    MONTHLY_CONNECTHUB_MD_REPORT;

    public String getCronExpression(){
        return switch (this) {
            case CONNECTHUB_DFS -> "0 0 9 * * ?";
            case MONTHLY_CONNECTHUB_MD_REPORT -> "0 0 12 * * ?";
        };
    }
}
