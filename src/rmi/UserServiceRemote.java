package rmi;

import model.User;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for User Service
 * All methods are remotely callable from other JVMs
 */
public interface UserServiceRemote extends Remote {
    boolean registerUser(String username, String email, String password) throws RemoteException;

    User loginUser(String email, String password) throws RemoteException;

    User getUserByEmail(String email) throws RemoteException;

    User getUserById(int userId) throws RemoteException;

    boolean emailExists(String email) throws RemoteException;

    String ping() throws RemoteException;
}