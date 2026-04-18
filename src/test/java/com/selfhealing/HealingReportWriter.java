package com.selfhealing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class HealingReportWriter {
    private static final String FILE_PATH = "healing-report.md";

    static {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) {
                Files.write(Paths.get(FILE_PATH), "# AI Self-Healing Report\n\n".getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logStable(String stepId, String locator) {
        String log = String.format("- **%s** [%s]: Stable element located successfully. Locator: `%s`\n", 
            LocalDateTime.now(), stepId, locator);
        appendToFile(log);
    }

    public static void logHealed(String stepId, String oldLocator, String newLocator, double confidence) {
        String log = String.format("- **%s** [%s]: 🩺 **Healed!** Old: `%s` -> New: `%s` (Confidence: %.2f)\n", 
            LocalDateTime.now(), stepId, oldLocator, newLocator, confidence);
        appendToFile(log);
    }

    private static void appendToFile(String content) {
        try {
            Files.write(Paths.get(FILE_PATH), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
