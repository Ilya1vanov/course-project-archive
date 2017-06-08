package com.ilya.ivanov.service.file;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by ilya on 6/5/17.
 */
public class SimpleFIleConflictContext implements FileConflictContext {
    private boolean replaceExisting;

    private Supplier<Boolean> renameIfExist = () -> false;

    private Function<String, String> renameStrategy = (s) -> s;

    SimpleFIleConflictContext(boolean replaceExisting) {
        this.replaceExisting = replaceExisting;
    }

    public static SimpleFIleConflictContext create(boolean replaceExisting) {
        return new SimpleFIleConflictContext(replaceExisting);
    }

    @Override
    public boolean replaceExisting() {
        return replaceExisting;
    }

    @Override
    public boolean renameIfExist() {
        return renameIfExist.get();
    }

    @Override
    public String rename(String filename) {
        return renameStrategy.apply(filename);
    }

    public void setRenameIfExist(Supplier<Boolean> renameIfExist) {
        this.renameIfExist = renameIfExist;
    }

    public void setRenameStrategy(Function<String, String> renameStrategy) {
        this.renameStrategy = renameStrategy;
    }
}
