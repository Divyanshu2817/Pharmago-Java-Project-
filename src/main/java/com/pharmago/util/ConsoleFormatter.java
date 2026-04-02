package com.pharmago.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ConsoleFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    private ConsoleFormatter() {
    }

    public static String titleBlock(String title, String subtitle) {
        return """
                ============================================================
                                %s
                            %s
                ============================================================
                """.formatted(title, subtitle);
    }

    public static String section(String title) {
        return "\n-------------------- " + title + " --------------------";
    }

    public static String formatDate(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    public static String money(BigDecimal amount) {
        return "Rs. " + MONEY_FORMAT.format(amount);
    }
}
