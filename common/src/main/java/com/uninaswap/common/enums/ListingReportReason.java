package com.uninaswap.common.enums;

public enum ListingReportReason {
    INAPPROPRIATE_CONTENT("listing.report.reason.inappropriate.content"),
    MISLEADING_INFORMATION("listing.report.reason.misleading.information"),
    PROHIBITED_ITEMS("listing.report.reason.prohibited.items"),
    DUPLICATE_LISTING("listing.report.reason.duplicate.listing"),
    INCORRECT_CATEGORY("listing.report.reason.incorrect.category"),
    COUNTERFEIT_ITEMS("listing.report.reason.counterfeit.items"),
    OVERPRICED("listing.report.reason.overpriced"),
    SPAM("listing.report.reason.spam"),
    COPYRIGHT_VIOLATION("listing.report.reason.copyright.violation"),
    OTHER("listing.report.reason.other");

    private final String messageKey;

    ListingReportReason(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDisplayName() {
        return messageKey;
    }
}