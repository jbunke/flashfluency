package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.cli.ExceptionMessenger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIOHelper {
    public static void deleteFileFootprintFromSystem(final String filepath) {
        try {
            Path path = FileSystems.getDefault().getPath(filepath);

            if (path.toFile().exists())
                Files.delete(path);
        } catch (IOException e) {
            ExceptionMessenger.deliver(
                    "The file footprint \"" + filepath +
                            "\" was not deleted from the system directory.",
                    false
            );
        }
    }
}
