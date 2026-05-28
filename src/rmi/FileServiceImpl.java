package rmi;

import dao.FileDAO;
import model.FileDownload;
import model.FileInfo;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import util.AppConfig;
import util.FileValidationUtil;

/**
 * RMI Implementation of File Service
 * This object runs on the server and handles remote calls from clients
 */
public class FileServiceImpl extends UnicastRemoteObject implements FileServiceRemote {
    private static final long serialVersionUID = 1L;
    private FileDAO fileDAO = new FileDAO();
    private static final long MAX_UPLOAD_BYTES = 20L * 1024L * 1024L; // 20 MB safety cap

    public FileServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<FileInfo> getFilesByUser(int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: getFilesByUser(" + userId + ")");
            List<FileInfo> files = fileDAO.getFilesByUser(userId);
            System.out.println("[RMI SERVER] ✅ Returning " + files.size() + " files");
            return files;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in getFilesByUser: " + e.getMessage());
            throw new RemoteException("Error fetching files", e);
        }
    }

    @Override
    public FileInfo uploadFile(String originalFilename, byte[] data, int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: uploadFile(" + originalFilename + ") for user " + userId);

            String sanitizedFilename = FileValidationUtil.sanitizeUploadName(originalFilename);
            FileValidationUtil.validateUploadName(sanitizedFilename);

            if (data == null || data.length == 0) {
                throw new RemoteException("Empty upload data");
            }
            if (data.length > MAX_UPLOAD_BYTES) {
                throw new RemoteException("File too large for RMI upload (max 20MB)");
            }

            Path storageRoot = AppConfig.getStorageRoot();
            Files.createDirectories(storageRoot);

            String storedFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedFilename;
            Path targetPath = storageRoot.resolve(storedFileName);

            Files.write(targetPath, data);

            FileInfo info = fileDAO.saveFileAndReturn(sanitizedFilename, storedFileName, targetPath.toString(),
                    data.length, userId);
            if (info == null) {
                // Roll back the stored file if metadata fails
                Files.deleteIfExists(targetPath);
                throw new RemoteException("Failed to save file metadata");
            }

            System.out.println("[RMI SERVER] ✅ Upload complete: " + storedFileName);
            return info;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in uploadFile: " + e.getMessage());
            throw new RemoteException("Error uploading file", e);
        }
    }

    @Override
    public FileDownload downloadFile(int fileId, int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: downloadFile(" + fileId + ") for user " + userId);

            FileInfo file = fileDAO.getFileByIdForUser(fileId, userId);
            if (file == null) {
                throw new RemoteException("File not found or access denied");
            }

            Path sourcePath = Paths.get(file.getFilepath());
            byte[] data = Files.readAllBytes(sourcePath);
            System.out.println("[RMI SERVER] ✅ Download bytes: " + data.length);
            return new FileDownload(file.getFilename(), data);
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in downloadFile: " + e.getMessage());
            throw new RemoteException("Error downloading file", e);
        }
    }

    @Override
    public boolean saveFile(String originalFilename, String storedFilename,
            String filepath, long filesize, int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: saveFile(" + originalFilename + ") for user " + userId);
            boolean result = fileDAO.saveFile(originalFilename, storedFilename, filepath, filesize, userId);
            System.out.println("[RMI SERVER] ✅ File saved: " + (result ? "Success" : "Failed"));
            return result;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in saveFile: " + e.getMessage());
            throw new RemoteException("Error saving file", e);
        }
    }

    @Override
    public boolean deleteFile(int fileId, int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: deleteFile(" + fileId + ") for user " + userId);
            boolean result = fileDAO.deleteFile(fileId, userId);
            System.out.println("[RMI SERVER] ✅ File deleted: " + (result ? "Success" : "Failed"));
            return result;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in deleteFile: " + e.getMessage());
            throw new RemoteException("Error deleting file", e);
        }
    }

    @Override
    public boolean deleteFileAndStorage(int fileId, int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: deleteFileAndStorage(" + fileId + ") for user " + userId);

            FileInfo file = fileDAO.getFileByIdForUser(fileId, userId);
            if (file == null) {
                return false;
            }

            // Delete metadata first or file first? Prefer file first to avoid orphan files.
            Path path = Paths.get(file.getFilepath());
            Files.deleteIfExists(path);

            boolean deleted = fileDAO.deleteFile(fileId, userId);
            System.out.println("[RMI SERVER] ✅ File delete result: " + deleted);
            return deleted;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in deleteFileAndStorage: " + e.getMessage());
            throw new RemoteException("Error deleting file", e);
        }
    }

    @Override
    public String generateShareToken(int fileId, int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: generateShareToken(" + fileId + ")");
            String token = fileDAO.generateShareToken(fileId, userId);
            System.out.println("[RMI SERVER] ✅ Token generated: " + token);
            return token;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in generateShareToken: " + e.getMessage());
            throw new RemoteException("Error generating share token", e);
        }
    }

    @Override
    public boolean revokeShareToken(int fileId, int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: revokeShareToken(" + fileId + ")");
            boolean revoked = fileDAO.revokeShareToken(fileId, userId);
            System.out.println("[RMI SERVER] ✅ Token revoked: " + revoked);
            return revoked;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in revokeShareToken: " + e.getMessage());
            throw new RemoteException("Error revoking share token", e);
        }
    }

    @Override
    public List<FileInfo> getSharedFilesByUser(int userId) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: getSharedFilesByUser(" + userId + ")");
            List<FileInfo> files = fileDAO.getSharedFilesByUser(userId);
            System.out.println("[RMI SERVER] ✅ Returning " + files.size() + " shared files");
            return files;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in getSharedFilesByUser: " + e.getMessage());
            throw new RemoteException("Error fetching shared files", e);
        }
    }

    @Override
    public FileInfo getFileByShareToken(String token) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: getFileByShareToken(" + token + ")");
            FileInfo file = fileDAO.getFileByShareToken(token);
            System.out.println("[RMI SERVER] ✅ File retrieved by token");
            return file;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in getFileByShareToken: " + e.getMessage());
            throw new RemoteException("Error getting file by token", e);
        }
    }

    @Override
    public FileDownload downloadFileByShareToken(String token) throws RemoteException {
        try {
            System.out.println("[RMI SERVER] Remote call: downloadFileByShareToken(" + token + ")");

            FileInfo file = fileDAO.getFileByShareToken(token);
            if (file == null) {
                throw new RemoteException("Invalid or expired share token");
            }

            Path sourcePath = Paths.get(file.getFilepath());
            byte[] data = Files.readAllBytes(sourcePath);
            System.out.println("[RMI SERVER] ✅ Download by token bytes: " + data.length);
            return new FileDownload(file.getFilename(), data);
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[RMI SERVER] ❌ Error in downloadFileByShareToken: " + e.getMessage());
            throw new RemoteException("Error downloading shared file", e);
        }
    }

    @Override
    public String ping() throws RemoteException {
        String response = "FileService is alive at " + System.currentTimeMillis();
        System.out.println("[RMI SERVER] Ping received from client: " + response);
        return response;
    }
}