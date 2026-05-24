package dao;

import model.FileInfo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    private static boolean shareTokenColumnChecked = false;

    // Save uploaded file metadata
    public boolean saveFile(String filename, String filepath, long filesize, int userId) {
        String sql = "INSERT INTO files(filename, filepath, filesize, uploaded_by) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filename);
            ps.setString(2, filepath);
            ps.setLong(3, filesize);
            ps.setInt(4, userId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all files for a user
    public List<FileInfo> getFilesByUser(int userId) {
        List<FileInfo> files = new ArrayList<>();
        String sql = "SELECT * FROM files WHERE uploaded_by = ? ORDER BY upload_date DESC";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                FileInfo file = new FileInfo(
                        rs.getInt("id"),
                        rs.getString("filename"),
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

    // Delete file
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

    // Generate Share Token - IMPROVED
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

    // Get file by share token
    public FileInfo getFileByShareToken(String token) {
        String sql = "SELECT * FROM files WHERE share_token = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ensureShareTokenColumn(conn);

            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FileInfo(
                        rs.getInt("id"),
                        rs.getString("filename"),
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

    private void ensureShareTokenColumn(Connection conn) throws SQLException {
        if (shareTokenColumnChecked) {
            return;
        }

        String addColumnSql = "ALTER TABLE files ADD COLUMN share_token VARCHAR(64) NULL";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(addColumnSql);
        } catch (SQLException e) {
            // MySQL error code 1060 = duplicate column name.
            if (e.getErrorCode() != 1060) {
                throw e;
            }
        }

        shareTokenColumnChecked = true;
    }
}