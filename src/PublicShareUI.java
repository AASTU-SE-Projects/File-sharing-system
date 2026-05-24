import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import dao.FileDAO;
import model.FileInfo;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PublicShareUI extends Application {

    private FileDAO fileDAO = new FileDAO();

    @Override
    public void start(Stage stage) {

        Label title = new Label("Public File Share");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label subtitle = new Label("Enter Share Token to Download File");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        TextField tokenField = new TextField();
        tokenField.setPromptText("Paste share token here (e.g. a1b2c3d4e5f6g7h8)");
        tokenField.setPrefWidth(400);

        Button downloadButton = new Button("Download File");
        downloadButton.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12px 30px;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(title, subtitle, tokenField, downloadButton, statusLabel);

        // Download Action
        downloadButton.setOnAction(e -> {
            String token = tokenField.getText().trim();

            if (token.isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("❌ Please enter a share token");
                return;
            }

            FileInfo file = fileDAO.getFileByShareToken(token);

            if (file != null) {
                try {
                    String downloadDir = "downloads/public/";
                    new File(downloadDir).mkdirs();

                    String destination = downloadDir + file.getFilename();
                    Files.copy(Paths.get(file.getFilepath()), Paths.get(destination));

                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel
                            .setText("✅ Download Successful!\nFile saved to: downloads/public/" + file.getFilename());
                } catch (Exception ex) {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("❌ Download failed: " + ex.getMessage());
                }
            } else {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("❌ Invalid or expired share token");
            }
        });

        Scene scene = new Scene(layout, 550, 380);
        stage.setTitle("Public Share - Campus File Sharing");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}