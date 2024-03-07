package org.os.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandReader {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public List<String> readCommand() throws IOException {
        String command = reader.readLine();
        return List.of(command.split(" "));
    }
}
