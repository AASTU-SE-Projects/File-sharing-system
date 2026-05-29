package dao;

import model.User;
import java.sql.*;
import util.PasswordUtil;

public class UserDAO {

    // Login Method
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                boolean matches = PasswordUtil.verifyPassword(password, storedPassword);

                if (!matches) {
                    return null;
                }

                if (!PasswordUtil.isBcryptHash(storedPassword)) {
                    String hashedPassword = PasswordUtil.hashPassword(password);
                    upgradePasswordHash(conn, rs.getInt("id"), hashedPassword);
                }

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(null);
                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User loginUser(String email, String password) {
        return login(email, password);
    }

    // Register Method (we'll use later)
    public boolean register(String username, String email, String password) {
        String sql = "INSERT INTO users(username, email, password) VALUES (?, ?, ?)";


        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashedPassword = PasswordUtil.hashPassword(password);

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean registerUser(String username, String email, String password) {
        return register(username, email, password);
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(null);
                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(null);
                return user;
            }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void upgradePasswordHash(Connection conn, int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}