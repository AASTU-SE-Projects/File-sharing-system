import javafx.application.Platform;
import javafx.stage.Stage;

public class MultiUserTest {

    public static void main(String[] args) {

        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "false");

        System.out.println("=====================================");
        System.out.println("🚀 Starting Multi-User Test Mode");
        System.out.println("Two instances will be launched...");
        System.out.println("=====================================");

        try {
            Platform.setImplicitExit(false);
            Platform.startup(() -> {
                try {
                    System.out.println("✅ Starting First Instance (User 1)");
                    new LoginUI().start(new Stage());

                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(() -> {
                                try {
                                    System.out.println("✅ Starting Second Instance (User 2)");
                                    new LoginUI().start(new Stage());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}