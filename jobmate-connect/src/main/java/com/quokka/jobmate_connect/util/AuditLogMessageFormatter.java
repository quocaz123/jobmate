package com.quokka.jobmate_connect.util;

import com.quokka.jobmate_connect.constant.AuditAction;

public final class AuditLogMessageFormatter {

    private AuditLogMessageFormatter() {
    }

    public static String format(AuditAction action, String subject, String... details) {
        StringBuilder builder = new StringBuilder();
        appendSegment(builder, action != null ? action.getLabel() : null);
        appendSegment(builder, subject);
        if (details != null) {
            for (String detail : details) {
                appendSegment(builder, detail);
            }
        }
        if (builder.length() == 0) {
            if (action != null) {
                return action.getLabel() != null ? action.getLabel() : action.name();
            }
            return "UNSPECIFIED ACTION";
        }
        return builder.toString();
    }

    private static void appendSegment(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" - ");
        }
        builder.append(value.trim());
    }
}
