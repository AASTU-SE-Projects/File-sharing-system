package rmi;

import model.FileDownload;
import model.FileInfo;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for File Service
 * All methods are remotely callable from other JVMs
 */
public interface FileServiceRemote extends Remote {
    List<FileInfo> getFilesByUser(int userId) throws RemoteException;

    FileInfo uploadFile(String originalFilename, byte[] data, int userId) throws RemoteException;

    FileDownload downloadFile(int fileId, int userId) throws RemoteException;

    boolean saveFile(String originalFilename, String storedFilename,
            String filepath, long filesize, int userId) throws RemoteException;

    boolean deleteFile(int fileId, int userId) throws RemoteException;

    boolean deleteFileAndStorage(int fileId, int userId) throws RemoteException;

    String generateShareToken(int fileId, int userId) throws RemoteException;

    boolean revokeShareToken(int fileId, int userId) throws RemoteException;

    List<FileInfo> getSharedFilesByUser(int userId) throws RemoteException;

    FileInfo getFileByShareToken(String token) throws RemoteException;

    FileDownload downloadFileByShareToken(String token) throws RemoteException;

    String ping() throws RemoteException;
}