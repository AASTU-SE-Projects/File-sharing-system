package dao;

import model.FileDownload;
import model.FileInfo;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    private static boolean shareTokenColumnChecked = false;
    private static boolean storedFilenameColumnChecked = false;

    // Save uploaded file metadata with UUID-based filename
    public boolean saveFile(String originalFilename, String storedFilename, String filepath, long filesize,
            int userId) {
        String sql = "INSERT INTO files(filename, stored_filename, filepath, filesize, uploaded_by) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureStoredFilenameColumn(conn);

            ps.setString(1, originalFilename);
            ps.setString(2, storedFilename);
            ps.setString(3, filepath);
            ps.setLong(4, filesize);
            ps.setInt(5, userId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public FileInfo saveFileAndReturn(String originalFilename, String storedFilename, String filepath, long filesize,
            int userId) {
        String sql = "INSERT INTO files(filename, stored_filename, filepath, filesize, uploaded_by) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ensureStoredFilenameColumn(conn);

            ps.setString(1, originalFilename);
            ps.setString(2, storedFilename);
            ps.setString(3, filepath);
            ps.setLong(4, filesize);
            ps.setInt(5, userId);

            int rows = ps.executeUpdate();
            if (rows <= 0) {
                return null;
            }

            int id = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }

            return new FileInfo(id, originalFilename, storedFilename, filepath, filesize, userId,
                    new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Legacy method - kept for backward compatibility
    public boolean saveFile(String filename, String filepath, long filesize, int userId) {
        String storedFilename = java.util.UUID.randomUUID().toString().substring(0, 8) + "_" + filename;
        return saveFile(filename, storedFilename, filepath, filesize, userId);
    }

    // Get all files for a user
    public List<FileInfo> getFilesByUser(int userId) {
        List<FileInfo> files = new ArrayList<>();
        String sql = "SELECT * FROM files WHERE uploaded_by = ? ORDER BY upload_date DESC";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureStoredFilenameColumn(conn);

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                FileInfo file = new FileInfo(
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("stored_filename"),
                        rs.getString("filepath"),
                        rs.getLong("filesize"),
                        rs.getInt("uploaded_by"),
                        rs.getTimestamp("upload_date"));
                files.add(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    // Delete file and associated metadata
    public boolean deleteFile(int fileId, int userId) {
        String sql = "DELETE FROM files WHERE id = ? AND uploaded_by = ?";
        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fileId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public FileInfo getFileByIdForUser(int fileId, int userId) {
        String sql = "SELECT * FROM files WHERE id = ? AND uploaded_by = ? LIMIT 1";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureStoredFilenameColumn(conn);

            ps.setInt(1, fileId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FileInfo(
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("stored_filename"),
                        rs.getString("filepath"),
                        rs.getLong("filesize"),
                        rs.getInt("uploaded_by"),
                        rs.getTimestamp("upload_date"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Generate Share Token
    public String generateShareToken(int fileId, int userId) {
        String token = java.util.UUID.randomUUID().toString().substring(0, 16);
        String sql = "UPDATE files SET share_token = ? WHERE id = ? AND uploaded_by = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureShareTokenColumn(conn);

            ps.setString(1, token);
            ps.setInt(2, fileId);
            ps.setInt(3, userId);

            if (ps.executeUpdate() > 0) {
                return token;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean revokeShareToken(int fileId, int userId) {
        String sql = "UPDATE files SET share_token = NULL WHERE id = ? AND uploaded_by = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureShareTokenColumn(conn);

            ps.setInt(1, fileId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<FileInfo> getSharedFilesByUser(int userId) {
        List<FileInfo> files = new ArrayList<>();
        String sql = "SELECT * FROM files WHERE uploaded_by = ? AND share_token IS NOT NULL ORDER BY upload_date DESC";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureShareTokenColumn(conn);
            ensureStoredFilenameColumn(conn);

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                FileInfo file = new FileInfo(
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("stored_filename"),
                        rs.getString("filepath"),
                        rs.getLong("filesize"),
                        rs.getInt("uploaded_by"),
                        rs.getTimestamp("upload_date"));
                files.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    // Get file by share token
    public FileInfo getFileByShareToken(String token) {
        String sql = "SELECT * FROM files WHERE share_token = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureShareTokenColumn(conn);
            ensureStoredFilenameColumn(conn);

            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FileInfo(
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("stored_filename"),
                        rs.getString("filepath"),
                        rs.getLong("filesize"),
                        rs.getInt("uploaded_by"),
                        rs.getTimestamp("upload_date"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public FileDownload downloadFileByShareToken(String token) {
        FileInfo file = getFileByShareToken(token);
        if (file == null) {
            return null;
        }

        try {
            byte[] data = Files.readAllBytes(Paths.get(file.getFilepath()));
            return new FileDownload(file.getFilename(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void ensureShareTokenColumn(Connection conn) throws SQLException {
        if (shareTokenColumnChecked) {
            return;
        }

        String addColumnSql = "ALTER TABLE files ADD COLUMN share_token VARCHAR(64) NULL";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(addColumnSql);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                throw e;
            }
        }

        shareTokenColumnChecked = true;
    }

    private void ensureStoredFilenameColumn(Connection conn) throws SQLException {
        if (storedFilenameColumnChecked) {
            return;
        }

        String addColumnSql = "ALTER TABLE files ADD COLUMN stored_filename VARCHAR(255) NULL";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(addColumnSql);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                throw e;
            }
        }

        storedFilenameColumnChecked = true;
    }
}