package util;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

public final class FileValidationUtil {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".exe", ".dll", ".bat", ".cmd", ".com", ".msi", ".ps1", ".vbs", ".js", ".jse",
            ".wsf", ".scr", ".pif", ".cpl", ".jar", ".lnk", ".hta", ".reg", ".sh", ".msix");

    private FileValidationUtil() {
    }

    public static String sanitizeUploadName(String originalFilename) {
        if (originalFilename == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String normalized = Path.of(originalFilename).getFileName().toString().trim();
        if (normalized.isEmpty() || normalized.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        return normalized;
    }

    public static void validateUploadName(String originalFilename) {
        String sanitized = sanitizeUploadName(originalFilename);
        String lower = sanitized.toLowerCase(Locale.ROOT);

        for (String extension : BLOCKED_EXTENSIONS) {
            if (lower.endsWith(extension)) {
                throw new IllegalArgumentException("Uploads with this file type are not allowed");
            }
        }
    }
}