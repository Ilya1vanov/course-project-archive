package com.ilya.ivanov.data.model;

import org.apache.log4j.Logger;

import javax.persistence.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by ilya on 5/19/17.
 */
@SuppressWarnings("unused")
@Entity
@Table(name = "files")
public class FileEntity {
    private static final Logger log = Logger.getLogger(FileEntity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @ManyToOne
    private FileEntity parent;

    @OneToMany(mappedBy="parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Column(name = "children")
    private Set<FileEntity> children;

    @Column(name = "filename")
    private String filename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "last_modified")
    @Temporal(value = TemporalType.DATE)
    private Date lastModified;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file")
    private byte[] file;

    public FileEntity(FileEntity parent) {
        this.parent = parent;
    }

    private FileEntity(FileEntity parent, String filename) {
        this(parent);
        this.filename = filename;
    }

    private FileEntity(FileEntity parent, List<FileEntity> children, String filename) {
        this(parent, filename);
        this.children.addAll(children);
    }

    private FileEntity(FileEntity parent, String filename, Long fileSize, Date lastModified, byte[] file) {
        this(parent, filename);
        this.fileSize = fileSize;
        this.lastModified = lastModified;
        this.file = file;
    }

    {
        if (children == null)
            this.children = new HashSet<>();
        if (file == null)
            this.file = new byte[] {};
        if (fileSize == null)
            fileSize = 0L;
        if (lastModified == null)
            this.lastModified = new Date();
        checkRep();
    }

    public static FileEntity createDirectory(FileEntity parent, List<FileEntity> children, String filename) {
        return new FileEntity(parent, children, filename);
    }

    public static FileEntity createFile(FileEntity parent, String filename, Long fileSize, Date updated, byte[] file) {
        return new FileEntity(parent, filename, fileSize, updated, file);
    }

    private void checkRep() {
        assert filename != null;
        assert !filename.isEmpty(): "Filename is empty";
        assert children != null;
        assert fileSize != null;
        assert lastModified != null;
        assert file != null;

        assert parent.isDirectory(): "Parent entity is not a directory";
        assert parent.getChildren().contains(this): "Parent doesn't contain this as children";
        if (isFile()) {
            assert children.size() == 0: "File has not empty children list";
        } else if (isDirectory()) {
            assert file.length == 0: "Directory has non zero-length file field";
            assert fileSize.equals(0L): "Directory has non zero file size";
        } else
            assert false: "Cannot determine type of entity";
    }

    public Long getId() {
        return id;
    }

    public FileEntity getParent() {
        return parent;
    }

    public void setParent(FileEntity parent) {
        if (parent != null)
            parent.children.remove(this);
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
        checkRep();
    }

    public Set<FileEntity> getChildren() {
        return children;
    }

    public void addChildren(FileEntity children) {
        this.children.add(children);
        if (children.parent != this)
            children.parent = this;
        checkRep();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        checkRep();
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        checkRep();
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        checkRep();
    }

    public byte[] getFile() throws DataFormatException, IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(file.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        log.debug("Compressed: " +this.file.length + " B");
        log.debug("Original: " + output.length + " B");
        return output;
    }

    public void setFile(byte[] file) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(file.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        this.file = outputStream.toByteArray();
        log.debug("Original: " + file.length + " B");
        log.debug("Compressed: " + this.file.length + " B");
        checkRep();
    }

    public boolean isFile() {
        return file.length != 0;
    }

    public boolean isDirectory() {
        return !isFile();
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean isRoot() {
        return hasParent();
    }
}
