package model;

import java.io.Serializable;

public class FileDownload implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String filename;
    private final byte[] data;

    public FileDownload(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }
}

