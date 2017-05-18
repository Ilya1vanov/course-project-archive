package com.ilya.ivanov.data.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by ilya on 5/19/17.
 */
@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @ManyToOne
    private FileEntity parent;

    @OneToMany(mappedBy="parent", fetch = FetchType.LAZY)
    @Column(name = "children")
    private List<FileEntity> children;


}
