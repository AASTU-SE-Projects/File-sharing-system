package rmi;

import util.AppConfig;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Server - Starts RMI registry and binds remote objects
 * Run this on the server machine first
 * 
 * Usage: java -cp bin rmi.RMIServer
 */
public class RMIServer {
    private static final String FILE_SERVICE_NAME = "FileService";
    private static final String USER_SERVICE_NAME = "UserService";

    public static void main(String[] args) {
        try {
            String rmiHost = AppConfig.getRmiHost();
            int rmiPort = AppConfig.getRmiPort();

            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║     🚀 RMI SERVER - STARTING        ║");
            System.out.println("╚════════════════════════════════════╝\n");

            // Step 1: Create RMI Registry
            System.out.println("[1/4] Creating RMI Registry on port " + rmiPort + "...");
            try {
                LocateRegistry.createRegistry(rmiPort);
                System.out.println("✅ RMI Registry created successfully\n");
            } catch (RemoteException e) {
                System.out.println("⚠️  Registry already exists, reusing existing registry\n");
            }

            // Step 2: Get Registry reference
            System.out.println("[2/4] Getting RMI Registry reference...");
            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            System.out.println("✅ Registry reference obtained\n");

            // Step 3: Create and bind FileService
            System.out.println("[3/4] Creating and binding FileService...");
            FileServiceRemote fileService = new FileServiceImpl();
            registry.rebind(FILE_SERVICE_NAME, fileService);
            System.out.println("✅ FileService bound successfully");
            System.out.println("   Location: rmi://" + rmiHost + ":" + rmiPort + "/" + FILE_SERVICE_NAME + "\n");

            // Step 4: Create and bind UserService
            System.out.println("[4/4] Creating and binding UserService...");
            UserServiceRemote userService = new UserServiceImpl();
            registry.rebind(USER_SERVICE_NAME, userService);
            System.out.println("✅ UserService bound successfully");
            System.out.println("   Location: rmi://" + rmiHost + ":" + rmiPort + "/" + USER_SERVICE_NAME + "\n");

            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║  ✅ RMI SERVER READY FOR CLIENTS   ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.println("║  Services Running:                 ║");
            System.out.println("║  - FileService (port " + rmiPort + ")            ║");
            System.out.println("║  - UserService (port " + rmiPort + ")            ║");
            System.out.println("║                                    ║");
            System.out.println("║  Waiting for client connections... ║");
            System.out.println("║  (Press Ctrl+C to stop)            ║");
            System.out.println("╚════════════════════════════════════╝\n");

            // Keep server running indefinitely
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
            }

        } catch (Exception e) {
            System.err.println("\n❌ Fatal Error starting RMI Server:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        }
    }
}