package main.java.ui;
import main.java.dao.UserDAO;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.model.User;

public class LoginUI extends Application {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void start(Stage primaryStage) {
        Label title = new Label("Campus File Sharing");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");

        Label subtitle = new Label("Student Portal");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setStyle("-fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px;");

        Button loginButton = new Button("Login");
        loginButton.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12px 40px;");

        Button registerButton = new Button("Create New Account");
        registerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2980b9; -fx-font-size: 14px;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");

        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        layout.getChildren().addAll(title, subtitle, emailField, passwordField, loginButton, registerButton,
                statusLabel);

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
                statusLabel.setText("Please enter email and password.");
                return;
            }

            User user = userDAO.login(email, password);
            if (user != null) {
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 13px;");
                statusLabel.setText("Login Success - Opening Dashboard...");

                Stage dashboardStage = new Stage();
                new DashboardUI(user).start(dashboardStage);
                primaryStage.close();
            } else {
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
                statusLabel.setText("Invalid email or password.");
            }
        });

        registerButton.setOnAction(e -> {
            primaryStage.close();
            new RegisterUI().start(new Stage());
        });

        Scene scene = new Scene(layout, 480, 460);
        primaryStage.setTitle("Login - Campus File Sharing");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }}

    
    

    

    
    
            