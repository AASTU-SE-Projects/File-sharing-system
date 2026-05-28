package main.java.ui;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.model.User;

public class ProfileUI extends Application {

    private User currentUser;

    public ProfileUI(User user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage stage) {

        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("Username: " + currentUser.getUsername());
        Label emailLabel = new Label("Email: " + currentUser.getEmail());
        usernameLabel.setStyle("-fx-font-size: 16px;");
        emailLabel.setStyle("-fx-font-size: 16px;");

        Button changePasswordBtn = new Button("Change Password");
        Button backButton = new Button("Back to Dashboard");

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(title, usernameLabel, emailLabel,
                changePasswordBtn, backButton);

        // Back to Dashboard
        backButton.setOnAction(e -> {
            stage.close();
            new DashboardUI(currentUser).start(new Stage());
        });

        // Change Password (Simple version)
        changePasswordBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Change Password");
            dialog.setHeaderText("Enter New Password");
            dialog.showAndWait().ifPresent(newPassword -> {
                if (!newPassword.isEmpty()) {
                    // TODO: Add update password in UserDAO later
                    showAlert("Success", "Password changed successfully! (Demo)");
                }
            });
        });

        Scene scene = new Scene(layout, 500, 400);
        stage.setTitle("Profile - " + currentUser.getUsername());
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle(title);
        alert.showAndWait();
    }
}