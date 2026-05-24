import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import dao.FileDAO;
import model.FileInfo;
import model.User;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DashboardUI extends Application {

    private User currentUser;
    private TableView<FileInfo> tableView = new TableView<>();
    private FileDAO fileDAO = new FileDAO();
    private TextField searchField = new TextField();

    public DashboardUI(User user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage stage) {

        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Search Bar
        searchField.setPromptText("Search files by name...");
        searchField.setPrefWidth(350);

        HBox searchBox = new HBox(10, new Label("🔍 Search:"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button uploadButton = new Button("📤 Upload File");
        Button downloadButton = new Button("⬇️ Download");
        Button deleteButton = new Button("🗑️ Delete");
        Button shareButton = new Button("🔗 Share");
        Button profileButton = new Button("👤 Profile");
        Button logoutButton = new Button("Logout");

        HBox buttonBox = new HBox(12, uploadButton, downloadButton, deleteButton,
                shareButton, profileButton, logoutButton);
        buttonBox.setAlignment(Pos.CENTER);

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

        tableView.getColumns().addAll(nameCol, sizeCol, dateCol);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(welcomeLabel, searchBox, buttonBox, tableView);

        // Load files
        refreshFileList();

        // Search Listener
        searchField.textProperty().addListener((obs, old, newVal) -> filterFiles(newVal));

        // Button Actions
        uploadButton.setOnAction(e -> uploadFile(stage));
        downloadButton.setOnAction(e -> downloadSelectedFile());
        deleteButton.setOnAction(e -> deleteSelectedFile());
        shareButton.setOnAction(e -> shareSelectedFile());
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
        tableView.getItems().addAll(fileDAO.getFilesByUser(currentUser.getId()));
    }

    private void filterFiles(String keyword) {
        tableView.getItems().clear();
        List<FileInfo> allFiles = fileDAO.getFilesByUser(currentUser.getId());

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
    }

    private void uploadFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String uploadDir = "uploads/";
                new File(uploadDir).mkdirs();

                String filepath = uploadDir + selectedFile.getName();
                Files.copy(selectedFile.toPath(), Paths.get(filepath));

                boolean saved = fileDAO.saveFile(
                        selectedFile.getName(),
                        filepath,
                        selectedFile.length(),
                        currentUser.getId());

                if (saved) {
                    showAlert("Success", "File uploaded successfully!");
                    refreshFileList();
                }
            } catch (Exception ex) {
                showAlert("Error", "Upload failed: " + ex.getMessage());
            }
        }
    }

    private void downloadSelectedFile() {
        FileInfo selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a file!");
            return;
        }
        try {
            String downloadDir = "downloads/";
            new File(downloadDir).mkdirs();
            Files.copy(Paths.get(selected.getFilepath()),
                    Paths.get(downloadDir + selected.getFilename()));
            showAlert("Success", "File downloaded successfully!");
        } catch (Exception e) {
            showAlert("Error", "Download failed");
        }
    }

    private void deleteSelectedFile() {
        FileInfo selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a file!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this file?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (fileDAO.deleteFile(selected.getId(), currentUser.getId())) {
                new File(selected.getFilepath()).delete();
                showAlert("Success", "File deleted successfully");
                refreshFileList();
            }
        }
    }

    private void shareSelectedFile() {
        FileInfo selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a file first!");
            return;
        }

        String token = fileDAO.generateShareToken(selected.getId(), currentUser.getId());
        if (token != null) {
            showAlert("✅ Share Link Generated",
                    "File: " + selected.getFilename() + "\n\n" +
                            "Share Token: " + token + "\n\n" +
                            "Give this token to others to download the file.");
        } else {
            showAlert("Error", "Failed to generate share link");
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
}