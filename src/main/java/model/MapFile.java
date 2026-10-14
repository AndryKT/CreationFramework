package model;

import java.util.Map;

public class MapFile {
    private String nameFile;
    private String extensionFile;
    private byte[] contentFile;

    public MapFile(String nameFile, String extensionFile, byte[] contentFile) {
        this.nameFile = nameFile;
        this.extensionFile = extensionFile;
        this.contentFile = contentFile;
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
}
