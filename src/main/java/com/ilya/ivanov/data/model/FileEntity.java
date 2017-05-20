package com.ilya.ivanov.data.model;

import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity
@Table(name = "files")
@Component
@Lazy
@Scope("prototype")
@Configurable
public class FileEntity {
    private static final Logger log = Logger.getLogger(FileEntity.class);

    private static final FileEntity PLACEHOLDER = new FileEntity(null, "...");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @ManyToOne
    private FileEntity parent;

    @OneToMany(mappedBy="parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Column(name = "children")
    private List<FileEntity> children;

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

    private FileEntity() {}

    private FileEntity(FileEntity parent, String filename) {
        this.setParent(parent);
        this.filename = filename;
    }

    private FileEntity(FileEntity parent, List<FileEntity> children, String filename) {
        this(parent, filename);
        this.children = new ArrayList<>();
        if (children != null && !children.isEmpty())
            this.addChildren(children);
        else
            this.fileSize = 0L;
    }

    private FileEntity(FileEntity parent, String filename, byte[] file) throws IOException {
        this(parent, filename);
        this.setFile(file != null ? file : new byte[]{});
        this.lastModified = new Date();
    }

    public static FileEntity createDirectory(FileEntity parent, List<FileEntity> children, String filename) {
        return new FileEntity(parent, children, filename);
    }

    public static FileEntity createFile(FileEntity parent, String filename, byte[] file) throws IOException {
        return new FileEntity(parent, filename, file);
    }

    public static FileEntity getPlaceholder() {
        return PLACEHOLDER;
    }

    @PostConstruct
    private void checkRep() {
        assert filename != null;
        assert !filename.isEmpty(): "Filename is empty";
        assert fileSize != null;

        if (parent != null) {
            assert parent.isDirectory() : "Parent entity is not a directory";
            assert parent.getChildren().contains(this) : "Parent doesn't contain this as children";
        }
        if (isFile()) {
            assert file != null;
            assert lastModified != null;
//            assert parent != null;
            assert children == null: "File has not null children";
        } else if (isDirectory()) {
            assert children != null;
            assert lastModified == null: "Directory has not null last modified";
            assert file == null: "Directory has not null file field";
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
        if (this.parent != null)
            this.parent.children.remove(this);
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
//            checkRep();
        }
    }

    public List<FileEntity> getChildren() {
        return children == null ? null : ImmutableList.copyOf(children);
    }

    public void addChild(FileEntity child) {
        if (isDirectory()) {
            this.children.add(child);
            if (child.parent != this)
                child.parent = this;
            this.fileSize += child.getFileSize();
            checkRep();
        } else
            throw new RuntimeException("Not a directory");
    }

    public void addChildren(Collection<FileEntity> children) {
        if (isDirectory()) {
            this.children.addAll(children);
            children.forEach(c -> {
                if (c.parent != this) {
                    c.parent = this;
                }
            });
            this.fileSize += children.stream().mapToLong(FileEntity::getFileSize).sum();
//            checkRep();
        } else
            throw new RuntimeException("Not a directory");
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

    public Date getLastModified() {
        return (Date) lastModified.clone();
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        checkRep();
    }

    public void touch() {
        setLastModified(new Date());
    }

    public byte[] getFile() throws DataFormatException, IOException {
        if (isFile()) {
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
            log.debug("Compressed: " + this.file.length + " B");
            log.debug("Original: " + output.length + " B");
            return output;
        } else
            return this.file;
    }

    public void setFile(byte[] file) throws IOException {
        if (isFile()) {
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
            this.fileSize = (long) this.file.length;
            log.debug("Original: " + file.length + " B");
            log.debug("Compressed: " + this.file.length + " B");
        } else
            throw new IOException("Not a file");
    }

    public boolean isFile() {
        return !isDirectory();
    }

    public boolean isDirectory() {
        return children != null;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean isRoot() {
        return hasParent();
    }
}
