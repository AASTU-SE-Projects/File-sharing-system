package rmi;

import util.AppConfig;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Client - Utility for connecting to remote services
 * Used by client applications (LoginUI, DashboardUI, etc.)
 */
public class RMIClient {
    private static final String FILE_SERVICE_NAME = "FileService";
    private static final String USER_SERVICE_NAME = "UserService";

    private static FileServiceRemote fileService;
    private static UserServiceRemote userService;
    private static boolean isConnected = false;

    /**
     * Initialize RMI connection
     * Call this once when application starts
     * 
     * @throws RemoteException   If connection fails
     * @throws NotBoundException If service not found
     */
    public static void initialize() throws RemoteException, NotBoundException {
        if (isConnected) {
            System.out.println("ℹ️  Already connected to RMI Server");
            return;
        }

        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   🔗 RMI CLIENT - CONNECTING       ║");
        System.out.println("╚════════════════════════════════════╝\n");

        try {
            String rmiHost = AppConfig.getRmiHost();
            int rmiPort = AppConfig.getRmiPort();

            // Step 1: Locate Registry
            System.out.println("[1/3] Connecting to RMI Registry at " + rmiHost + ":" + rmiPort + "...");
            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            System.out.println("✅ Connected to RMI Registry\n");

            // Step 2: Look up FileService
            System.out.println("[2/3] Looking up FileService...");
            fileService = (FileServiceRemote) registry.lookup(FILE_SERVICE_NAME);
            System.out.println("✅ FileService located\n");

            // Step 3: Look up UserService
            System.out.println("[3/3] Looking up UserService...");
            userService = (UserServiceRemote) registry.lookup(USER_SERVICE_NAME);
            System.out.println("✅ UserService located\n");

            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║  ✅ RMI CONNECTED SUCCESSFULLY    ║");
            System.out.println("╚════════════════════════════════════╝\n");

            isConnected = true;

        } catch (RemoteException | NotBoundException e) {
            System.err.println("\n❌ RMI Connection Failed:");
            System.err.println("   " + e.getMessage());
            System.err.println("\n   Make sure RMIServer is running:");
            System.err.println("   $ java -cp bin rmi.RMIServer");
            throw e;
        }
    }

    /**
     * Get FileService reference
     */
    public static FileServiceRemote getFileService() throws RemoteException, NotBoundException {
        if (fileService == null) {
            initialize();
        }
        return fileService;
    }

    /**
     * Get UserService reference
     */
    public static UserServiceRemote getUserService() throws RemoteException, NotBoundException {
        if (userService == null) {
            initialize();
        }
        return userService;
    }

    /**
     * Check if connected to RMI
     */
    public static boolean isConnected() {
        try {
            if (fileService != null && userService != null) {
                fileService.ping();
                userService.ping();
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Attempt to reconnect
     */
    public static void reconnect() throws RemoteException, NotBoundException {
        System.out.println("\n🔄 Attempting to reconnect to RMI Server...");
        fileService = null;
        userService = null;
        isConnected = false;
        initialize();
    }

    /**
     * Get connection status details
     */
    public static String getStatus() {
        if (isConnected) {
            return "Connected to RMI (FileService & UserService)";
        } else {
            return "Disconnected - RMI Server not available";
        }
    }
}