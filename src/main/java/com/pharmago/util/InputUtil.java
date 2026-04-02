package com.pharmago.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

public class InputUtil {
    private final Scanner scanner;

    public InputUtil(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid integer value.");
            }
        }
    }

    public BigDecimal readDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid decimal amount.");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (Exception exception) {
                System.out.println("Enter date in YYYY-MM-DD format.");
            }
        }
    }

    public boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim().toLowerCase();
            if ("yes".equals(value) || "y".equals(value) || "true".equals(value)) {
                return true;
            }
            if ("no".equals(value) || "n".equals(value) || "false".equals(value)) {
                return false;
            }
            System.out.println("Enter yes or no.");
        }
    }

    public String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
