package org.os.userland;

import org.os.core.*;
import org.os.util.CommandReader;
import org.os.util.MemoryVisualiser;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.out;

public class ComputerInterface {
    public static final int REAL_MEMORY_SIZE = 4624;
    public static final int PAGINATION_TABLE_SIZE = 256; // 16 pages, 16 words per page (4 bytes per word)
    public static final int VM_ADDRESS = 256; // PROC 16 pages, 16 words per page

    public ComputerInterface() {
        out.println("=== Computer started ===");
        Cpu cpu = new Cpu();
        RealMemory realMemory = new RealMemory(REAL_MEMORY_SIZE);
        PaginationTable paginationTable = new PaginationTable(realMemory, PAGINATION_TABLE_SIZE);
        MemoryManager memoryManager = new MemoryManager(cpu, realMemory, paginationTable);
        RealMachine realMachine = new RealMachine(realMemory, cpu, memoryManager, paginationTable);
        CommandReader commandReader = new CommandReader();

        prompt(commandReader, realMachine);
    }

    private void prompt(CommandReader commandReader, RealMachine realMachine) {
        Logger log = Logger.getLogger(ComputerInterface.class.getName());
        try {
            do {
                List<String> commands = commandReader.readCommand();
                String first = commands.getFirst();
                boolean status = switch (first) {
                    case "load" -> realMachine.load(commands.get(1));
                    case "clear" -> realMachine.clear(Integer.parseInt(commands.get(1)));
                    case "run" -> realMachine.run(Integer.parseInt(commands.get(1)));
                    case "memory" -> showMemoryTable(commandReader, realMachine);
                    case "cls" -> clearConsole();
                    default -> throw new IllegalStateException("Unexpected value: " + first);
                };

                if (!status) {
                    log.severe("Error: " + first);
                } else {
                    out.println("Success: " + first);
                }
            } while (true);
        } catch (Exception e) {
            log.severe("Error: " + e.getMessage());
        }
    }

    public boolean showMemoryTable(CommandReader commandReader, RealMachine realMachine) throws Exception {
        String input;
        do {
            clearConsole();
            out.printf("Choose an option:%n1. Pagination Table%n2. Virtual Machines%n3. Virtual Memory%n4. Exit%n");
            input = commandReader.readCommand().getFirst();
            MemoryVisualiser memoryVisualiser = new MemoryVisualiser(realMachine.getRealMemory().getMemory());
            switch (input) {
                case "1" -> memoryVisualiser.showPagination();
                case "2" -> memoryVisualiser.showVirtualMachines();
                case "3" -> memoryVisualiser.showVirtualMemory();
                case "4" -> prompt(commandReader, realMachine);
                default -> out.println("Invalid input");
            }
        } while (!"4".equals(input));

        return true;
    }

    private boolean clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                Runtime.getRuntime().exec("clear");
            }
            return true;
        } catch (IOException | InterruptedException e) {
            out.println("Error: " + e.getMessage());
        }
        return false;
    }
}
