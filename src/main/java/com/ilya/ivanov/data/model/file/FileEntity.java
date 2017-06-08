package com.ilya.ivanov.data.model.file;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.SortNatural;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.io.*;
import java.net.URI;
import java.text.DateFormat;
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
public class FileEntity implements Comparable<FileEntity> {
    private static final Logger log = Logger.getLogger(FileEntity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @ManyToOne
    private FileEntity parent;

    @OneToMany(mappedBy="parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Column(name = "children")
    @SortNatural
    private SortedSet<FileEntity> children;

    @Column(name = "filename")
    private String filename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "last_modified")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastModified;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file")
    private byte[] file;

    @Transient
    private boolean check;

    @Transient
    private static DateFormat format = DateFormat.getDateTimeInstance();

    private FileEntity() {}

    private FileEntity(FileEntity parent, String filename) {
        this.parent = parent;
        this.filename = filename;
        this.check = true;
    }

    private FileEntity(FileEntity parent, List<FileEntity> children, String filename) {
        this(parent, filename);
        this.children = new TreeSet<>();
        if (children != null && !children.isEmpty())
            this.addChildren(children);
        else
            this.fileSize = 0L;
    }

    private FileEntity(FileEntity parent, String filename, byte[] file) throws IOException {
        this(parent, filename);
        this.file = new byte[]{};
        this.setFile(file != null ? file : this.file);
        this.lastModified = new Date();
    }

    public static FileEntity createDirectory(FileEntity parent, List<FileEntity> children, String filename) {
        return new FileEntity(parent, children, filename);
    }

    public static FileEntity createFile(FileEntity parent, String filename, byte[] file) throws IOException {
        return new FileEntity(parent, filename, file);
    }

    public static FileEntity createFile(FileEntity parent, File file) throws IOException {
        return createFile(parent, file.getName(), IOUtils.toByteArray(new FileInputStream(file)));
    }

    public FileEntity createDirectory(String filename) {
        return this.createDirectory(null, filename);
    }

    public FileEntity createDirectory(List<FileEntity> children, String filename) {
        checkDirectory();
        return createDirectory(this, children, filename);
    }

    public FileEntity createFile(String filename, byte[] file) throws IOException {
        checkDirectory();
        return createFile(this, filename, file);
    }

    public FileEntity createFile(File file) throws IOException {
        checkDirectory();
        return createFile(this, file);
    }

    private void checkDirectory() {
        if (!this.isDirectory())
            throw new IllegalStateException("This is not directory");
    }

    @PostConstruct
    private void initialize() {
        this.setParent(parent);
        if (check)
            checkRep();
    }

    private void checkRep() {
        assert filename != null;
        assert !filename.isEmpty() : "Filename is empty";
        assert fileSize != null;

        if (parent != null) {
            assert parent.isDirectory() : "Parent entity is not a directory";
            assert Lists.newArrayList(parent.children).contains(this) : "Parent doesn't contain this as children";
        }
        if (isFile()) {
            assert file != null;
            assert lastModified != null;
            assert children == null || children.isEmpty() : "File has not empty children";
        } else if (isDirectory()) {
            assert children != null;
            assert lastModified == null : "Directory has not null last modified";
            assert file == null : "Directory has not null file field";
        } else
            assert false : "Cannot determine type of entity";
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return pathTraversal(this).substring(5);
    }

    private static String pathTraversal(FileEntity fileEntity) {
        if (fileEntity == null) return "";
        else return pathTraversal(fileEntity.getParent()) + "/" + fileEntity.getFilename();
    }

    public FileEntity getParent() {
        return parent;
    }

    public void resetParent() {
        this.setParent(null);
    }

    public void setParent(FileEntity parent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = parent;
        if (parent != null) {
            this.parent.addChild(this);
        }
    }

    public SortedSet<FileEntity> getChildren() {
        return children;/* == null ? null : ImmutableList.copyOf(children);*/
    }

    public void addChild(FileEntity child) {
        checkDirectory();
        this.children.add(child);
        recalculateFileSize(this, child.fileSize);
        if (child.parent != this)
            child.parent = this;
        checkRep();
    }

    public void removeChild(FileEntity child) {
        checkDirectory();
        if (this.children.contains(child)) {
            this.children.remove(child);
            child.parent = null;
            recalculateFileSize(this, -child.fileSize);
        }
        checkRep();
    }

    private static void recalculateFileSize(FileEntity file, long fileSize) {
        if (file != null) {
            file.fileSize += fileSize;
            recalculateFileSize(file.getParent(), fileSize);
        }
    }

    public void addChildren(Collection<FileEntity> children) {
        children.forEach(this::addChild);
    }

    public void removeChildren(Collection<FileEntity> children) {
        children.forEach(this::removeChild);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        if (this.isFile())
            this.touch();
        checkRep();
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileSizeFormat() {
        int unit = 1024;
        if (fileSize < unit) return fileSize + " B";
        int exp = (int) (Math.log(fileSize) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + "i";
        return String.format("%.1f %sB", fileSize / Math.pow(unit, exp), pre);
    }

    public Date getLastModified() {
        if (isFile())
            return (Date) lastModified.clone();
        else
            return null;
    }

    public String getLastModifiedFormat() {
        return lastModified == null ? "" : format.format(lastModified);
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
        return file != null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEntity that = (FileEntity) o;
        return (id != null ? id.equals(that.id) : that.id == null) && (filename != null ? filename.equals(that.filename) : that.filename == null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(FileEntity o) {
        if (this == o) return 0;
        if (this.isDirectory() && o.isDirectory() || this.isFile() && o.isFile()) {
            if (this.filename != null && o.filename != null)
                return this.filename.compareTo(o.filename);
            else
                return this.id.compareTo(o.id);
        } else
            return this.isDirectory() ? 1 : -1;
    }
}
