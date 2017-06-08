package com.ilya.ivanov.data.model.file;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ilya on 6/5/17.
 */
public class FileDto {
    private String filePath;

    private String filename;

    private Long fileSize;

    private Date lastModified;

    public FileDto(FileEntity fileEntity) {
        this.filename = fileEntity.getFilename();
        this.fileSize = fileEntity.getFileSize();
        this.lastModified = fileEntity.getLastModified();
        this.filePath = fileEntity.getPath();
    }

    public FileDto(String filePath, String filename, Long fileSize, Date lastModified) {
        this.filePath = filePath;
        this.filename = filename;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }

    private String getFileSizeFormat() {
        int unit = 1024;
        if (fileSize < unit) return fileSize + " B";
        int exp = (int) (Math.log(fileSize) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + "i";
        return String.format("%.1f %sB", fileSize / Math.pow(unit, exp), pre);
    }

    public String getFilename() {
        return filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "Filepath = '" + filePath + '\'' +
                ", size = " + getFileSizeFormat() +
                ", last modified = " + lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDto fileDto = (FileDto) o;
        return filePath.equals(fileDto.filePath) && filename.equals(fileDto.filename) && fileSize.equals(fileDto.fileSize);
    }

    @Override
    public int hashCode() {
        int result = filePath.hashCode();
        result = 31 * result + filename.hashCode();
        result = 31 * result + fileSize.hashCode();
        return result;
    }
}
