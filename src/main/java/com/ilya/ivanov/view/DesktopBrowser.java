package com.ilya.ivanov.view;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Илья on 24.03.2017.
 * Not-instantiated class, used to browse files using desktop utilities and resources,
 * it uses default programs of desktop to open files
 */
public class DesktopBrowser implements Runnable {
    /** Object of users desktop, which allows to open files by its default programs */
    private final static Desktop desktop = Desktop.getDesktop();

    /** private constructor of class, which implement non-instantiation */
    public DesktopBrowser(File file){
        this.file = file;
    }

    /* file to open */
    private File file;

    /** Opens files on users desktop by default programmes, that are associated with files extension. */
    @Override
    public void run() {
        try {
            desktop.open(file);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
