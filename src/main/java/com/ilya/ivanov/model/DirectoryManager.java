package com.ilya.ivanov.model;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by ilya on 5/20/17.
 */
@Component
public class DirectoryManager {
    /* log4j logger */
    private static final Logger log = Logger.getLogger(DirectoryManager.class);



    public File createTempDirectory(String dir) throws IOException {
        Path temp = Files.createTempDirectory(Paths.get(dir), null);
        File tempDir = new File(temp.toString());
        tempDir.deleteOnExit();
        log.info("Temp directory " + tempDir.getPath() + " successfully created.");
        return tempDir;
    }
}
