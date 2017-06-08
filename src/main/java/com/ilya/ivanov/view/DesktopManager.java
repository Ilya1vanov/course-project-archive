package com.ilya.ivanov.view;

import org.springframework.stereotype.Component;

import javax.transaction.NotSupportedException;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Callable;

/**
 * Created by ilya on 6/5/17.
 */
public class DesktopManager implements Callable<Void> {
    private final File file;

    private DesktopManager(File file) {
        this.file = file;
    }

    public static DesktopManager open(File file) {
        return new DesktopManager(file);
    }

    public File getFile() {
        return file;
    }

    @Override
    public Void call() throws Exception {
        if (!Desktop.isDesktopSupported())
            throw new NotSupportedException("Open operation is not supported on this configurations");
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN))
            throw new NotSupportedException("Open operation is not supported on this configurations");
        desktop.open(file);
        return null;
    }
}
