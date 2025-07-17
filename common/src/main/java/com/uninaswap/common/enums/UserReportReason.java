package com.uninaswap.common.enums;

public enum UserReportReason {
    SCAMMER("user.report.reason.scammer"),
    INAPPROPRIATE_BEHAVIOR("user.report.reason.inappropriate.behavior"),
    FAKE_PROFILE("user.report.reason.fake.profile"),
    HARASSMENT("user.report.reason.harassment"),
    SPAM("user.report.reason.spam"),
    FRAUD("user.report.reason.fraud"),
    IMPERSONATION("user.report.reason.impersonation"),
    OTHER("user.report.reason.other");

    private final String messageKey;

    UserReportReason(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDisplayName() {
        return messageKey;
    }
}