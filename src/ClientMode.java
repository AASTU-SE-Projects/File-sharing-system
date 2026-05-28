import util.LaunchMode;

public class ClientMode {

    public static void main(String[] args) {
        System.out.println("👤 Running as CLIENT / Second Instance");
        System.out.println("Use this to test as another student.");

        LoginUI.setLaunchMode(LaunchMode.CLIENT);
        LoginUI.main(args);
    }
}