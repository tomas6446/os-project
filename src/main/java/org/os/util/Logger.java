package org.os.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Logger {
    private final Path logDir;

    public Logger() {
        logDir = Paths.get("logs");
        createLogDir();
        clearLogs();
    }

    private void createLogDir() {
        try {
            if (!Files.exists(logDir)) {
                Files.createDirectory(logDir);
            }
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }

    private void clearLogs() {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDir)) {
            for (Path path : directoryStream) {
                deleteFile(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to clear log directory: " + e.getMessage());
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + path + " - " + e.getMessage());
        }
    }

    public void writeOutputToFile(int vmId, String output) {
        String fileName = "vm_" + vmId + ".txt";
        Path filePath = logDir.resolve(fileName);
        try (FileWriter writer = new FileWriter(filePath.toFile(), true)) {
            writer.write(output + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + filePath + " - " + e.getMessage());
        }
    }
}
