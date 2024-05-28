package org.os.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    public Logger() {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        clearLogs();
    }

    private void clearLogs() {
        File logDir = new File("logs");
        for (File file : logDir.listFiles()) {
            file.delete();
        }
    }

    public void writeOutputToFile(int vmId, String output) {
        String fileName = "logs/vm_" + vmId + ".txt";
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(output + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
