package org.os.userland;

import org.os.core.*;
import org.os.util.CommandReader;
import org.os.util.CpuVisualiser;
import org.os.util.MemoryVisualiser;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static java.lang.System.out;

public class ComputerInterface {
    public static final int REAL_MEMORY_SIZE = 4624;
    public static final int PAGINATION_TABLE_SIZE = 256; // 16 pages, 16 words per page (4 bytes per word)
    public static final int VM_ADDRESS = 256; // PROC 16 pages, 16 words per page
    public static final int CYCLES = 6;

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
            int debug = 0;
            do {
                List<String> commands = commandReader.readCommand();
                String first = commands.getFirst();
                switch (first) {
                    case "load" -> realMachine.load(commands.get(1));
                    case "clear" -> realMachine.clear(Integer.parseInt(commands.get(1)));
                    case "run" -> handleRun(realMachine, debug, commands);

                    case "debug" -> {
                        debug = Integer.parseInt(commands.get(1));
                        handleDebug(realMachine, debug);
                    }

                    case "memory" -> showMemoryTable(commandReader, realMachine);
                    case "cls" -> clearConsole();
                    default -> throw new IllegalStateException("Unexpected value: " + first);
                }
            } while (true);
        } catch (Exception e) {
            log.severe("Error: " + e.getMessage());
        }
    }

    private static void handleRun(RealMachine realMachine, int debug, List<String> commands) {
        if (debug == 1) {
            out.println("Write 'continue' to continue the program. Type 'exit' to exit the program.");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if ("continue".equalsIgnoreCase(input)) {
                realMachine.preRun(Integer.parseInt(commands.get(1)));
            } else if ("exit".equalsIgnoreCase(input)) {
                realMachine.virtualMachineInterrupt();
            }
        } else {
            realMachine.run(Integer.parseInt(commands.get(1)), CYCLES);
        }
    }

    private boolean handleDebug(RealMachine realMachine, int debug) {
        if (debug == 1) {
            out.println("Debug mode started.");
            return true;
        } else if (debug == 0) {
            out.println("Debug mode stopped.");
            realMachine.virtualMachineInterrupt();
            return false;
        } else {
            out.println("Invalid input");
        }
        return true;
    }

    public boolean showMemoryTable(CommandReader commandReader, RealMachine realMachine) throws Exception {
        String input;
        do {
            clearConsole();
            out.printf("Choose an option:%n1. Pagination Table%n2. Virtual Machines%n3. Virtual Memory%n4. Registers%n5. Exit%n" +
                    "Enter the number of the option: ");
            input = commandReader.readCommand().getFirst();
            MemoryVisualiser memoryVisualiser = new MemoryVisualiser(realMachine.getRealMemory().getMemory());
            CpuVisualiser cpuVisualiser = new CpuVisualiser(realMachine.getCpu());
            switch (input) {
                case "1" -> memoryVisualiser.showPagination();
                case "2" -> memoryVisualiser.showVirtualMachines();
                case "3" -> memoryVisualiser.showVirtualMemory();
                case "4" -> cpuVisualiser.showRegisters();
                case "5" -> prompt(commandReader, realMachine);
                default -> out.println("Invalid input");
            }
        } while (!"5".equals(input));

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
