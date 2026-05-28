import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.FileDownload;
import rmi.FileServiceRemote;
import rmi.RMIClient;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class PublicShareUI extends Application {

    private FileServiceRemote fileService;

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("🔗 [PublicShareUI] Attempting RMI connection...");
            RMIClient.initialize();
            fileService = RMIClient.getFileService();
            System.out.println("✅ [PublicShareUI] RMI connection successful - using remote services");
        } catch (Exception e) {
            System.out.println("⚠️  [PublicShareUI] RMI not available");
            System.out.println("   Error: " + e.getMessage());
            fileService = null;
        }

        Label title = new Label("Public File Share");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label subtitle = new Label("Enter Share Token to Download File");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label tokenLabel = new Label("Share Token");
        tokenLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField tokenField = new TextField();
        tokenField.setPromptText("Paste the token here and click Download");
        tokenField.setPrefWidth(420);
        tokenField.setPrefHeight(40);
        tokenField.setStyle("-fx-font-size: 14px; -fx-background-radius: 10; -fx-border-radius: 10;"
                + "-fx-border-color: #cfd8e3; -fx-background-color: #f8fbfe;");

        Button pasteButton = new Button("Paste from Clipboard");
        pasteButton.setStyle("-fx-background-color: #edf3f9; -fx-text-fill: #204d74;"
                + "-fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 10; -fx-cursor: hand;");
        pasteButton.setOnAction(e -> {
            String clipboardText = Clipboard.getSystemClipboard().getString();
            if (clipboardText != null && !clipboardText.trim().isEmpty()) {
                tokenField.setText(clipboardText.trim());
                tokenField.requestFocus();
                tokenField.selectAll();
            }
        });

        Button downloadButton = new Button("Download File");
        downloadButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px;"
                + "-fx-font-weight: 700; -fx-padding: 12px 30px; -fx-background-radius: 10; -fx-cursor: hand;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(title, subtitle, tokenLabel, tokenField, pasteButton, downloadButton, statusLabel);

        // Download Action
        downloadButton.setOnAction(e -> {
            String token = tokenField.getText().trim();

            if (token.isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("❌ Please enter a share token");
                return;
            }

            try {
                if (fileService == null || !RMIClient.isConnected()) {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText(
                            "❌ RMI Server is required for public downloads.\nStart RMIServer and set CAMPUS_SHARE_RMI_HOST.");
                    return;
                }

                System.out.println("📡 [PublicShareUI] Using RMI: Calling remote downloadFileByShareToken()");
                FileDownload downloaded = fileService.downloadFileByShareToken(token);
                if (downloaded == null || downloaded.getData() == null) {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("❌ Download failed: empty data returned");
                    return;
                }

                Path downloadDir = Paths.get("downloads/public");
                Files.createDirectories(downloadDir);

                Path destination = downloadDir.resolve(downloaded.getFilename());
                Files.write(destination, downloaded.getData());

                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("✅ Download Successful!\nFile saved to: " + destination);
            } catch (Exception ex) {
                System.err.println("❌ [PublicShareUI] Download error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("❌ Download failed: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(layout, 550, 380);
        stage.setTitle("Public Share - Campus File Sharing");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // If user explicitly requests console mode, skip JavaFX entirely
        if (args != null && args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            try {
                runConsoleFallback();
            } catch (Exception ex) {
                System.err.println("❌ [PublicShareUI] Console fallback failed: " + ex.getMessage());
                ex.printStackTrace();
            }
            return;
        }

        try {
            launch(args);
        } catch (Throwable t) {
            System.err.println(
                    "⚠️ [PublicShareUI] JavaFX failed to start, falling back to console mode: " + t.getMessage());
            try {
                runConsoleFallback();
            } catch (Exception ex) {
                System.err.println("❌ [PublicShareUI] Console fallback failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private static void runConsoleFallback() throws Exception {
        System.out.println("Public Share - Console Mode");
        System.out.println("Paste the share token and press Enter. Type 'exit' to quit.");

        // Initialize RMI client for console mode
        try {
            RMIClient.initialize();
        } catch (Exception e) {
            System.err.println("⚠️ [PublicShareUI] RMI initialization in console mode failed: " + e.getMessage());
        }

        rmi.FileServiceRemote svc = RMIClient.getFileService();
        Scanner scanner = new Scanner(System.in, "UTF-8");
        while (true) {
            System.out.print("Share Token> ");
            String token = scanner.nextLine();
            if (token == null)
                break;
            token = token.trim();
            if (token.equalsIgnoreCase("exit"))
                break;
            if (token.isEmpty()) {
                System.out.println("Please enter a token or 'exit'.");
                continue;
            }

            if (svc == null || !RMIClient.isConnected()) {
                System.err.println(
                        "❌ RMI Server is required for public downloads. Start RMIServer and set CAMPUS_SHARE_RMI_HOST.");
                continue;
            }

            try {
                FileDownload downloaded = svc.downloadFileByShareToken(token);
                if (downloaded == null || downloaded.getData() == null) {
                    System.out.println("Download failed: empty data returned");
                    continue;
                }

                Path downloadDir = Paths.get("downloads/public");
                Files.createDirectories(downloadDir);
                Path destination = downloadDir.resolve(downloaded.getFilename());
                Files.write(destination, downloaded.getData());
                System.out.println("✅ Download Successful! File saved to: " + destination.toAbsolutePath());
            } catch (Exception ex) {
                System.err.println("Download failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        scanner.close();
        System.out.println("Console mode exiting.");
    }
}