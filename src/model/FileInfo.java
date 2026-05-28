package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String filename; // Original filename (e.g., notes.pdf)
    private String storedFilename; // UUID-based stored name (e.g., 550e8400_notes.pdf)
    private String filepath; // Network or local path to actual file
    private long filesize;
    private int uploadedBy;
    private Timestamp uploadDate;

    // Constructors
    public FileInfo() {
    }

    public FileInfo(int id, String filename, String filepath, long filesize, int uploadedBy, Timestamp uploadDate) {
        this.id = id;
        this.filename = filename;
        this.filepath = filepath;
        this.filesize = filesize;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
    }

    public FileInfo(int id, String filename, String storedFilename, String filepath, long filesize, int uploadedBy,
            Timestamp uploadDate) {
        this.id = id;
        this.filename = filename;
        this.storedFilename = storedFilename;
        this.filepath = filepath;
        this.filesize = filesize;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }
}