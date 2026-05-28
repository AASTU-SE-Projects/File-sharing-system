import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import util.AppConfig;
import rmi.RMIServer;
import util.LaunchMode;

public class ServerMode {

    public static void main(String[] args) {
        System.out.println("🚀 Running as SERVER / Main Instance");
        System.out.println("You can use this for testing as primary user.");

        LoginUI.setLaunchMode(LaunchMode.SERVER);

        Thread serverBootstrap = new Thread(() -> RMIServer.main(new String[0]), "rmi-server-bootstrap");
        serverBootstrap.setDaemon(true);
        serverBootstrap.start();

        waitForRmiRegistry();

        LoginUI.main(args);
    }

    private static void waitForRmiRegistry() {
        String rmiHost = AppConfig.getRmiHost();
        int rmiPort = AppConfig.getRmiPort();

        for (int attempt = 0; attempt < 50; attempt++) {
            try {
                Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
                registry.list();
                System.out.println("✅ RMI registry is ready for server mode");
                return;
            } catch (Exception ignored) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        System.out.println("⚠️  RMI registry did not become ready in time; opening login screen anyway.");
    }
}