package org.os.userland;

import org.os.core.RealMachine;
import org.os.util.CommandReader;

import java.util.List;
import java.util.logging.Logger;

public class ComputerInterface {
    public ComputerInterface() {
        Logger log = Logger.getLogger(ComputerInterface.class.getName());
        try {
            System.out.println("=== Computer started ===");
            CommandReader commandReader = new CommandReader();
            List<String> commands = commandReader.readCommand();
            String first = commands.getFirst();

            RealMachine realMachine = new RealMachine();
            switch (first) {
                case "load" -> realMachine.load(commands.get(1));
                case "unload" -> realMachine.unload(Integer.parseInt(commands.get(1)));
                case "run" -> realMachine.run(Integer.parseInt(commands.get(1)));
                default -> throw new IllegalStateException("Unexpected value: " + first);
            }
        } catch (Exception e) {
            log.severe("Error: " + e.getMessage());
        }
    }
}
