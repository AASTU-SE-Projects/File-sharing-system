package main.java.model;

import java.sql.Timestamp;

public class FileInfo {
    private int id;
    private String filename;
    private String filepath;
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