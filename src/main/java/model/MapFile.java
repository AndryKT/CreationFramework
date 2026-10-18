package model;

import java.util.Map;

public class MapFile {
    private String nameFile;
    private String extensionFile;
    private byte[] contentFile;
    private String contentType;

    public MapFile(String nameFile, String extensionFile, byte[] contentFile, String contentType) {
        this.nameFile = nameFile;
        this.extensionFile = extensionFile;
        this.contentFile = contentFile;
        this.contentType = contentType;
    }

    public MapFile() {
    }   

    // Getters and setters
    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public String getExtensionFile() {
        return extensionFile;
    }

    public void setExtensionFile(String extensionFile) {
        this.extensionFile = extensionFile;
    }

    public byte[] getContentFile() {
        return contentFile;
    }

    public void setContentFile(byte[] contentFile) {
        this.contentFile = contentFile;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
