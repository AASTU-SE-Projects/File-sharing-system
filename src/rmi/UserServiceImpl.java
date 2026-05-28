package rmi;

import dao.UserDAO;
import model.User;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMI Implementation of User Service
 * This object runs on the server and handles remote calls from clients
 */
public class UserServiceImpl extends UnicastRemoteObject implements UserServiceRemote {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

    public UserServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean registerUser(String username, String email, String password) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: registerUser(" + email + ")");
            boolean result = userDAO.registerUser(username, email, password);
            System.out.println("[RMI SERVER] ✅ User registered: " + (result ? "Success" : "Failed"));
            return result;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in registerUser: " + e.getMessage());
            throw new RemoteException("Error registering user", e);
        }
    }

    @Override
    public User loginUser(String email, String password) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: loginUser(" + email + ")");
            User user = userDAO.loginUser(email, password);
            if (user != null) {
                System.out.println("[RMI SERVER] ✅ User logged in: " + user.getUsername());
            } else {
                System.out.println("[RMI SERVER] ⚠️  Login failed: Invalid credentials");
            }
            return user;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in loginUser: " + e.getMessage());
            throw new RemoteException("Error logging in", e);
        }
    }

    @Override
    public User getUserByEmail(String email) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: getUserByEmail(" + email + ")");
            User user = userDAO.getUserByEmail(email);
            System.out.println("[RMI SERVER] ✅ User retrieved");
            return user;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in getUserByEmail: " + e.getMessage());
            throw new RemoteException("Error getting user", e);
        }
    }

    @Override
    public User getUserById(int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: getUserById(" + userId + ")");
            User user = userDAO.getUserById(userId);
            System.out.println("[RMI SERVER] ✅ User retrieved by ID");
            return user;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in getUserById: " + e.getMessage());
            throw new RemoteException("Error getting user", e);
        }
    }

    @Override
    public boolean emailExists(String email) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: emailExists(" + email + ")");
            boolean exists = userDAO.emailExists(email);
            System.out.println("[RMI SERVER] ✅ Email exists: " + exists);
            return exists;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in emailExists: " + e.getMessage());
            throw new RemoteException("Error checking email", e);
        }
    }

    @Override
    public String ping() throws RemoteException {
        String response = "UserService is alive at " + System.currentTimeMillis();
        System.out.println("[RMI SERVER] Ping received from client: " + response);
        return response;
    }
}