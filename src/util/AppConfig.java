package util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {

    // LOCAL MODE: Default local storage
    private static final String DEFAULT_STORAGE_ROOT = "uploads";

    // NETWORK MODE: Change these to enable LAN-based shared storage
    // Example UNC path: \\SERVER-PC\\shared or \\192.168.1.10\\shared
    private static final String DEFAULT_NETWORK_SERVER = ""; // Empty = use local mode
    private static final String DEFAULT_NETWORK_SHARE = "uploads";

    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/campus_share";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "YUTI";
    private static final String DEFAULT_RMI_HOST = "localhost";
    private static final int DEFAULT_RMI_PORT = 1099;

    private AppConfig() {
    }

    /**
     * Get storage root path - can be local or network path (UNC)
     * 
     * Network mode example:
     * - Set environment: CAMPUS_SHARE_SERVER=192.168.1.10
     * - Returns a UNC path built from that server and share name
     * 
     * Local mode (default):
     * - Returns: uploads (relative path)
     */
    public static Path getStorageRoot() {
        String networkServer = getSetting("campusshare.networkServer", "CAMPUS_SHARE_SERVER", DEFAULT_NETWORK_SERVER);

        if (networkServer == null || networkServer.trim().isEmpty()) {
            // Local mode
            return Paths.get(getSetting("campusshare.storageRoot", "CAMPUS_SHARE_STORAGE_ROOT", DEFAULT_STORAGE_ROOT));
        } else {
            // Network mode - construct UNC path
            String networkShare = getSetting("campusshare.networkShare", "CAMPUS_SHARE_NETWORK_SHARE",
                    DEFAULT_NETWORK_SHARE);
            String uncPath = "\\\\" + networkServer + "\\" + networkShare;
            return Paths.get(uncPath);
        }
    }

    /**
     * Check if using network mode
     */
    public static boolean isNetworkMode() {
        String networkServer = getSetting("campusshare.networkServer", "CAMPUS_SHARE_SERVER", DEFAULT_NETWORK_SERVER);
        return networkServer != null && !networkServer.trim().isEmpty();
    }

    /**
     * Get server hostname/IP (for network mode)
     */
    public static String getNetworkServer() {
        return getSetting("campusshare.networkServer", "CAMPUS_SHARE_SERVER", DEFAULT_NETWORK_SERVER);
    }

    public static String getDbUrl() {
        return getSetting("campusshare.dbUrl", "CAMPUS_SHARE_DB_URL", DEFAULT_DB_URL);
    }

    public static String getDbUser() {
        return getSetting("campusshare.dbUser", "CAMPUS_SHARE_DB_USER", DEFAULT_DB_USER);
    }

    public static String getDbPassword() {
        return getSetting("campusshare.dbPassword", "CAMPUS_SHARE_DB_PASSWORD", DEFAULT_DB_PASSWORD);
    }

    public static String getRmiHost() {
        return getSetting("campusshare.rmiHost", "CAMPUS_SHARE_RMI_HOST", DEFAULT_RMI_HOST);
    }

    public static int getRmiPort() {
        String value = getSetting("campusshare.rmiPort", "CAMPUS_SHARE_RMI_PORT", String.valueOf(DEFAULT_RMI_PORT));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_RMI_PORT;
        }
    }

    private static String getSetting(String systemProperty, String environmentVariable, String defaultValue) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(environmentVariable);
        }

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }
}