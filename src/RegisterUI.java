import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import dao.UserDAO;
import rmi.RMIClient;
import rmi.UserServiceRemote;

public class RegisterUI extends Application {

    private final UserDAO userDAO = new UserDAO();
    private UserServiceRemote userService;

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("🔗 [RegisterUI] Attempting RMI connection...");
            RMIClient.initialize();
            userService = RMIClient.getUserService();
            System.out.println("✅ [RegisterUI] RMI connection successful - using remote services");
        } catch (Exception e) {
            System.out.println("⚠️  [RegisterUI] RMI not available, using local mode");
            System.out.println("   Error: " + e.getMessage());
            userService = null;
        }

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f7f9fc, #d9e6f2 55%, #bed6ea);");

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(34));
        card.setMaxWidth(430);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.93);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(26, 64, 96, 0.13);"
                + "-fx-effect: dropshadow(gaussian, rgba(18, 52, 86, 0.22), 28, 0.2, 0, 9);");

        Label miniHeading = new Label("New Account");
        miniHeading.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #395d7b;"
                + "-fx-background-color: #e6eef6; -fx-padding: 5 12 5 12; -fx-background-radius: 99;");

        Label title = new Label("Create New Account");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: 800; -fx-text-fill: #18344b;");

        Label subtitle = new Label("Join your class workspace securely.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #687a89;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(42);
        usernameField.setStyle("-fx-font-size: 14px; -fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #cad8e5; -fx-background-color: #f8fbfe;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(42);
        emailField.setStyle("-fx-font-size: 14px; -fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #cad8e5; -fx-background-color: #f8fbfe;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(42);
        passwordField.setStyle("-fx-font-size: 14px; -fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #cad8e5; -fx-background-color: #f8fbfe;");

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(44);
        registerButton.setStyle("-fx-background-color: linear-gradient(to right, #1d6fa6, #2458a7);"
                + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700;"
                + "-fx-background-radius: 12; -fx-cursor: hand;");

        Button backToLoginButton = new Button("Back to Login");
        backToLoginButton.setMaxWidth(Double.MAX_VALUE);
        backToLoginButton.setPrefHeight(40);
        backToLoginButton.setStyle("-fx-background-color: #edf3f9; -fx-text-fill: #204d74;"
                + "-fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 10; -fx-cursor: hand;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #b02a37; -fx-font-size: 12px;");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(miniHeading, title, subtitle, usernameField, emailField,
                passwordField, registerButton,
                backToLoginButton, statusLabel);
        card.getChildren().add(layout);
        root.getChildren().add(card);
        addCardEntrance(card);

        // Register Action
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("❌ All fields are required!");
                return;
            }

            try {
                boolean success;

                if (userService != null && RMIClient.isConnected()) {
                    System.out.println("📡 [RegisterUI] Using RMI: Calling remote registerUser()");
                    success = userService.registerUser(username, email, password);
                } else {
                    System.out.println("💾 [RegisterUI] Using Local: Calling local register()");
                    success = userDAO.register(username, email, password);
                }

                if (success) {
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("✅ Registration Successful! Please Login.");

                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                stage.close();
                                new LoginUI().start(new Stage());
                            });
                        }
                    }, 1500);
                } else {
                    statusLabel.setText("❌ Registration failed. Email may already exist.");
                }
            } catch (Exception ex) {
                System.err.println("❌ [RegisterUI] Registration error: " + ex.getMessage());
                statusLabel.setText("❌ Registration failed: " + ex.getMessage());
            }
        });

        backToLoginButton.setOnAction(e -> {
            stage.close();
            new LoginUI().start(new Stage());
        });

        Scene scene = new Scene(root, 560, 540);
        stage.setTitle("Register - Campus File Sharing");
        stage.setScene(scene);
        stage.show();
    }

    private void addCardEntrance(VBox card) {
        card.setOpacity(0);
        card.setScaleX(0.96);
        card.setScaleY(0.96);

        FadeTransition fade = new FadeTransition(Duration.millis(460), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(460), card);
        scale.setFromX(0.96);
        scale.setFromY(0.96);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}