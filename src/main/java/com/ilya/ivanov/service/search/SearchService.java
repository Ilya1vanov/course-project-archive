package com.ilya.ivanov.service.search;

import com.google.common.collect.Lists;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.repository.FileRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by ilya on 5/31/17.
 */
@Component
public class SearchService {
    private final FileRepository fileRepository;

    @Value("${ui.search.pageSize}")
    private Integer pageSize;

    private String query;

    private Slice<FileEntity>[] slices;

    private long count;

    private StringProperty currentNumberOfElements = new SimpleStringProperty(this, "currentNumberOfElements");

    @Autowired
    public SearchService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public List<FileEntity> startNewSearch(String query) {
        count = fileRepository.countByFilenameContaining(query);
        slices = new Slice[(int)count];
        this.query = query;
        return getSlice(0);
    }

    public List<FileEntity> getSlice(int page) {
        if (count == 0) return Lists.newArrayList();
        final PageRequest pageRequest = new PageRequest(page, pageSize);
        if (slices[page] == null) {
            slices[page] = fileRepository.findByFilenameContaining(query, pageRequest);
        }
        this.setCurrentResultsNumber(page);
        return slices[page].getContent();
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public int getPageCount() {
        return (int) count / pageSize + (count % pageSize == 0 ? 0 : 1);
    }

    public String getCurrentNumberOfElements() {
        return currentNumberOfElements.get();
    }

    public StringProperty currentNumberOfElementsProperty() {
        return currentNumberOfElements;
    }

    private void setCurrentResultsNumber(int index) {
        final int numberOfElements = slices[index].getNumberOfElements();
        this.currentNumberOfElements.set(Integer.toString(numberOfElements));
    }
}
