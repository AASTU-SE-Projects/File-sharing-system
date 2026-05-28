package main.java.ui;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.dao.UserDAO;

public class RegisterUI extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("Create New Account");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button registerButton = new Button("Register");
        Button backToLoginButton = new Button("Back to Login");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(title, usernameField, emailField,
                passwordField, registerButton,
                backToLoginButton, statusLabel);

        // Register Action
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("❌ All fields are required!");
                return;
            }

            UserDAO userDAO = new UserDAO();
            boolean success = userDAO.register(username, email, password);

            if (success) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("✅ Registration Successful! Please Login.");

                // Auto go back to login after 1.5 seconds
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
        });

        backToLoginButton.setOnAction(e -> {
            stage.close();
            new LoginUI().start(new Stage());
        });

        Scene scene = new Scene(layout, 420, 380);
        stage.setTitle("Register - Campus File Sharing");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}