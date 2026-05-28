import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import dao.FileDAO;
import model.FileDownload;
import model.FileInfo;
import model.User;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import util.AppConfig;
import util.FileValidationUtil;
import rmi.FileServiceRemote;
import rmi.RMIClient;

public class DashboardUI extends Application {

    private static final String DOWNLOAD_ROOT = "downloads";

    private User currentUser;
    private TableView<FileInfo> tableView = new TableView<>();
    private FileDAO fileDAO = new FileDAO();
    private FileServiceRemote fileService;
    private TextField searchField = new TextField();
    private Label viewModeLabel = new Label("View: All Files");
    private Label uploadStatusLabel = new Label();

    public DashboardUI(User user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage stage) {

        try {
            System.out.println("🔗 [DashboardUI] Attempting RMI connection...");
            RMIClient.initialize();
            fileService = RMIClient.getFileService();
            System.out.println("✅ [DashboardUI] RMI connection successful - using remote services");
        } catch (Exception e) {
            System.out.println("⚠️  [DashboardUI] RMI not available, using local mode");
            System.out.println("   Error: " + e.getMessage());
            fileService = null;
        }

        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #15364e;");

        Path storageRoot = AppConfig.getStorageRoot();
        String modeIndicator = AppConfig.isNetworkMode() ? " [Network Mode 🌐]" : " [Local Mode 💾]";
        Label storageLabel = new Label("Storage: " + storageRoot.toAbsolutePath() + modeIndicator);
        storageLabel.setStyle("-fx-text-fill: #5d7387; -fx-font-size: 12px; -fx-font-weight: 600;");

        // Search Bar
        searchField.setPromptText("Search files by name...");
        searchField.setPrefWidth(350);
        searchField.setPrefHeight(40);
        searchField.setStyle("-fx-font-size: 14px; -fx-background-color: #f7fafc; -fx-background-radius: 12;"
                + "-fx-border-color: #c8d7e6; -fx-border-radius: 12;");

        HBox searchBox = new HBox(10, new Label("🔍 Search:"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(6, 0, 0, 0));
        viewModeLabel.setStyle("-fx-text-fill: #173a56; -fx-font-size: 12px; -fx-font-weight: 700;"
                + "-fx-background-color: #e8f1f8; -fx-background-radius: 99; -fx-padding: 4 11 4 11;");
        uploadStatusLabel.setStyle("-fx-text-fill: #4c6a81; -fx-font-size: 12px;");

        // Buttons
        Button uploadButton = new Button("📤 Upload File");
        Button downloadButton = new Button("⬇️ Download");
        Button deleteButton = new Button("🗑️ Delete");
        Button shareButton = new Button("🔗 Share");
        Button sharedOnlyButton = new Button("📁 Shared Only");
        Button allFilesButton = new Button("📂 All Files");
        Button revokeShareButton = new Button("🚫 Revoke Share");
        Button profileButton = new Button("👤 Profile");
        Button logoutButton = new Button("Logout");

        styleMainButton(uploadButton);
        styleMainButton(downloadButton);
        styleDangerButton(deleteButton);
        styleMainButton(shareButton);
        styleGhostButton(sharedOnlyButton);
        styleGhostButton(allFilesButton);
        styleDangerButton(revokeShareButton);
        styleGhostButton(profileButton);
        styleGhostButton(logoutButton);

        HBox buttonBox = new HBox(12, uploadButton, downloadButton, deleteButton,
                shareButton, sharedOnlyButton, allFilesButton, revokeShareButton, profileButton, logoutButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(4, 0, 0, 0));

        // Table Columns
        TableColumn<FileInfo, String> nameCol = new TableColumn<>("File Name");
        nameCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFilename()));
        nameCol.setPrefWidth(300);

        TableColumn<FileInfo, Long> sizeCol = new TableColumn<>("Size (Bytes)");
        sizeCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFilesize()));

        TableColumn<FileInfo, String> dateCol = new TableColumn<>("Upload Date");
        dateCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUploadDate().toString()));

        tableView.getColumns().add(nameCol);
        tableView.getColumns().add(sizeCol);
        tableView.getColumns().add(dateCol);
        tableView.setStyle("-fx-background-color: #fdfefe; -fx-background-radius: 14; -fx-border-color: #d8e3ec;"
                + "-fx-border-radius: 14; -fx-padding: 6;");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fbff, #e5eff7 65%, #d4e3f0);");
        root.getChildren().addAll(welcomeLabel, storageLabel, searchBox, buttonBox, viewModeLabel, uploadStatusLabel,
                tableView);

        // Load files
        refreshFileList();

        // Search Listener
        searchField.textProperty().addListener((obs, old, newVal) -> filterFiles(newVal));

        // Button Actions
        uploadButton.setOnAction(e -> uploadFile(stage, uploadButton));
        downloadButton.setOnAction(e -> downloadSelectedFile());
        deleteButton.setOnAction(e -> deleteSelectedFile());
        shareButton.setOnAction(e -> shareSelectedFile());
        sharedOnlyButton.setOnAction(e -> showSharedFiles());
        allFilesButton.setOnAction(e -> {
            refreshFileList();
            viewModeLabel.setText("View: All Files");
        });
        revokeShareButton.setOnAction(e -> revokeShareSelectedFile());
        profileButton.setOnAction(e -> openProfile(stage));

        logoutButton.setOnAction(e -> {
            stage.close();
            new LoginUI().start(new Stage());
        });

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Campus File Sharing - Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void refreshFileList() {
        tableView.getItems().clear();
        try {
            List<FileInfo> files = getFilesForUser();
            tableView.getItems().addAll(files);
        } catch (Exception e) {
            System.err.println("❌ [DashboardUI] Error refreshing files: " + e.getMessage());
            showAlert("Error", "Failed to load files: " + e.getMessage());
        }
    }

    private List<FileInfo> getFilesForUser() throws Exception {
        if (fileService != null && RMIClient.isConnected()) {
            System.out.println("📡 [DashboardUI] Using RMI: Calling remote getFilesByUser()");
            return fileService.getFilesByUser(currentUser.getId());
        }
        System.out.println("💾 [DashboardUI] Using Local: Calling local getFilesByUser()");
        return fileDAO.getFilesByUser(currentUser.getId());
    }

    private List<FileInfo> getSharedFilesForUser() throws Exception {
        if (fileService != null && RMIClient.isConnected()) {
            System.out.println("📡 [DashboardUI] Using RMI: Calling remote getSharedFilesByUser()");
            return fileService.getSharedFilesByUser(currentUser.getId());
        }
        System.out.println("💾 [DashboardUI] Using Local: Calling local getSharedFilesByUser()");
        return fileDAO.getSharedFilesByUser(currentUser.getId());
    }

    private void filterFiles(String keyword) {
        tableView.getItems().clear();
        try {
            List<FileInfo> allFiles = getFilesForUser();

            if (keyword == null || keyword.trim().isEmpty()) {
                tableView.getItems().addAll(allFiles);
                return;
            }

            String search = keyword.toLowerCase().trim();
            for (FileInfo file : allFiles) {
                if (file.getFilename().toLowerCase().contains(search)) {
                    tableView.getItems().add(file);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [DashboardUI] Error filtering files: " + e.getMessage());
            showAlert("Error", "Search failed: " + e.getMessage());
        }
    }

    private void uploadFile(Stage stage, Button uploadButton) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to upload");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.doc", "*.txt"),
                new FileChooser.ExtensionFilter("Archives", "*.zip", "*.rar", "*.7z"));

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            uploadButton.setDisable(true);
            uploadStatusLabel.setText("Uploading " + selectedFile.getName() + " in background...");

            Task<String> uploadTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return executeUpload(selectedFile);
                }
            };

            uploadTask.setOnSucceeded(event -> {
                uploadButton.setDisable(false);
                uploadStatusLabel.setText("✅ " + uploadTask.getValue());
                refreshFileList();
            });

            uploadTask.setOnFailed(event -> {
                uploadButton.setDisable(false);
                Throwable error = uploadTask.getException();
                String message = error == null ? "Unknown upload error" : error.getMessage();
                uploadStatusLabel.setText("❌ Upload failed: " + message);
                showAlert("Error", "Upload failed: " + message);
            });

            Thread uploadThread = new Thread(uploadTask, "dashboard-upload-thread");
            uploadThread.setDaemon(true);
            uploadThread.start();
        }
    }

    private String executeUpload(File selectedFile) throws Exception {
        FileValidationUtil.validateUploadName(selectedFile.getName());

        if (fileService != null && RMIClient.isConnected()) {
            System.out.println("📡 [DashboardUI] Using RMI: Calling remote uploadFile()");
            byte[] data = Files.readAllBytes(selectedFile.toPath());
            FileInfo uploaded = fileService.uploadFile(selectedFile.getName(), data, currentUser.getId());
            if (uploaded == null) {
                throw new Exception("Upload failed (server returned null)");
            }
            return "File uploaded successfully via RMI server.";
        }

        System.out.println("💾 [DashboardUI] Using Local: Saving to storage root");
        Path storageRoot = AppConfig.getStorageRoot();
        Files.createDirectories(storageRoot);

        String storedFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + selectedFile.getName();
        Path targetPath = storageRoot.resolve(storedFileName);

        Files.copy(selectedFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        boolean saved = fileDAO.saveFile(
                selectedFile.getName(),
                storedFileName,
                targetPath.toString(),
                selectedFile.length(),
                currentUser.getId());

        if (!saved) {
            throw new Exception("Failed to save file metadata");
        }

        return "File uploaded successfully to " +
                (AppConfig.isNetworkMode() ? "shared network storage." : "local storage.");
    }

    private void downloadSelectedFile() {
        FileInfo selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a file!");
            return;
        }
        try {
            Path downloadDir = Paths.get(DOWNLOAD_ROOT);
            Files.createDirectories(downloadDir);

            if (fileService != null && RMIClient.isConnected()) {
                System.out.println("📡 [DashboardUI] Using RMI: Calling remote downloadFile()");
                FileDownload downloaded = fileService.downloadFile(selected.getId(), currentUser.getId());
                if (downloaded == null || downloaded.getData() == null) {
                    showAlert("Error", "Download failed (server returned empty data)");
                    return;
                }

                Path destPath = downloadDir.resolve(downloaded.getFilename());
                Files.write(destPath, downloaded.getData());
                showAlert("Success", "File downloaded successfully to: " + destPath.toAbsolutePath());
            } else {
                // Copy from network/local storage to local downloads folder
                Path sourcePath = Paths.get(selected.getFilepath());
                Path destPath = downloadDir.resolve(selected.getFilename());

                Files.copy(sourcePath, destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                showAlert("Success", "File downloaded successfully to: " + destPath.toAbsolutePath());
            }
        } catch (Exception e) {
            showAlert("Error", "Download failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteSelectedFile() {
        FileInfo selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a file!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete '" + selected.getFilename() + "' from storage? This action cannot be undone.");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                boolean deleted;

                if (fileService != null && RMIClient.isConnected()) {
                    System.out.println("📡 [DashboardUI] Using RMI: Calling remote deleteFileAndStorage()");
                    deleted = fileService.deleteFileAndStorage(selected.getId(), currentUser.getId());
                } else {
                    System.out.println("💾 [DashboardUI] Using Local: Calling local deleteFile()");
                    deleted = fileDAO.deleteFile(selected.getId(), currentUser.getId());
                }

                if (deleted) {
                    if (fileService == null || !RMIClient.isConnected()) {
                        // Delete actual file from storage (network or local)
                        File storageFile = new File(selected.getFilepath());
                        if (storageFile.exists()) {
                            storageFile.delete();
                        }
                    }
                    showAlert("Success", "File deleted successfully from storage");
                    refreshFileList();
                } else {
                    showAlert("Error", "Failed to delete file");
                }
            } catch (Exception e) {
                showAlert("Error", "Deletion failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void shareSelectedFile() {
        FileInfo selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a file first!");
            return;
        }

        try {
            String token;

            if (fileService != null && RMIClient.isConnected()) {
                System.out.println("📡 [DashboardUI] Using RMI: Calling remote generateShareToken()");
                token = fileService.generateShareToken(selected.getId(), currentUser.getId());
            } else {
                System.out.println("💾 [DashboardUI] Using Local: Calling local generateShareToken()");
                token = fileDAO.generateShareToken(selected.getId(), currentUser.getId());
            }

            if (token != null) {
                showAlert("✅ Share Link Generated",
                        "File: " + selected.getFilename() + "\n\n" +
                                "Share Token: " + token + "\n\n" +
                                "Give this token to others to download the file.");
            } else {
                showAlert("Error", "Failed to generate share link");
            }
        } catch (Exception e) {
            System.err.println("❌ [DashboardUI] Share error: " + e.getMessage());
            showAlert("Error", "Share failed: " + e.getMessage());
        }
    }

    private void showSharedFiles() {
        tableView.getItems().clear();
        try {
            List<FileInfo> files = getSharedFilesForUser();
            tableView.getItems().addAll(files);
            viewModeLabel.setText("View: Shared Only");
        } catch (Exception e) {
            System.err.println("❌ [DashboardUI] Shared list error: " + e.getMessage());
            showAlert("Error", "Failed to load shared files: " + e.getMessage());
        }
    }

    private void revokeShareSelectedFile() {
        FileInfo selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a file first!");
            return;
        }

        try {
            boolean revoked;
            if (fileService != null && RMIClient.isConnected()) {
                System.out.println("📡 [DashboardUI] Using RMI: Calling remote revokeShareToken()");
                revoked = fileService.revokeShareToken(selected.getId(), currentUser.getId());
            } else {
                System.out.println("💾 [DashboardUI] Using Local: Calling local revokeShareToken()");
                revoked = fileDAO.revokeShareToken(selected.getId(), currentUser.getId());
            }

            if (revoked) {
                showAlert("Success", "Share token revoked successfully.");
                refreshFileList();
            } else {
                showAlert("Error", "Failed to revoke share token.");
            }
        } catch (Exception e) {
            System.err.println("❌ [DashboardUI] Revoke share error: " + e.getMessage());
            showAlert("Error", "Revoke failed: " + e.getMessage());
        }
    }

    private void openProfile(Stage currentStage) {
        currentStage.close();
        new ProfileUI(currentUser).start(new Stage());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void styleMainButton(Button button) {
        button.setStyle("-fx-background-color: linear-gradient(to right, #1d6fa6, #2458a7);"
                + "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 700;"
                + "-fx-background-radius: 10; -fx-padding: 8 14 8 14; -fx-cursor: hand;");
    }

    private void styleDangerButton(Button button) {
        button.setStyle("-fx-background-color: linear-gradient(to right, #bb3e34, #932826);"
                + "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 700;"
                + "-fx-background-radius: 10; -fx-padding: 8 14 8 14; -fx-cursor: hand;");
    }

    private void styleGhostButton(Button button) {
        button.setStyle("-fx-background-color: #edf3f9; -fx-text-fill: #214e74;"
                + "-fx-font-size: 12px; -fx-font-weight: 700;"
                + "-fx-background-radius: 10; -fx-padding: 8 14 8 14; -fx-cursor: hand;");
    }
}