package dao;

import model.User;
import java.sql.*;
import util.PasswordUtil;

public class UserDAO {

    // Handles user authentication (login process)
    public User login(String email, String password) {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            // Check if user exists
            if (rs.next()) {

                String storedPassword = rs.getString("password");

                // Verify password using hashing utility
                boolean matches = PasswordUtil.verifyPassword(password, storedPassword);

                if (!matches) {
                    return null;
                }

                // Upgrade old password format to bcrypt if needed
                if (!PasswordUtil.isBcryptHash(storedPassword)) {
                    String hashedPassword = PasswordUtil.hashPassword(password);
                    upgradePasswordHash(conn, rs.getInt("id"), hashedPassword);
                }

                // Create and return user object (without password for security)
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

    // Wrapper method for login
    public User loginUser(String email, String password) {
        return login(email, password);
    }

    // Registers a new user into the system
    public boolean register(String username, String email, String password) {

        String sql = "INSERT INTO users(username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Hash password before storing
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

    // Wrapper method for register
    public boolean registerUser(String username, String email, String password) {
        return register(username, email, password);
    }

    // Retrieve user details using email
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

    // Retrieve user details using user ID
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

    // Check if email already exists in database
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

    // Upgrade old password format to bcrypt hash
    private void upgradePasswordHash(Connection conn, int userId, String hashedPassword) throws SQLException {

        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);

            ps.executeUpdate();
        }
    }
}