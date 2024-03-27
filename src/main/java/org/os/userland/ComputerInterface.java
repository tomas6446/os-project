package org.os.userland;

import org.os.core.*;
import org.os.util.CpuVisualiser;
import org.os.util.MemoryVisualiser;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.lang.System.out;

public class ComputerInterface {
    public static final int REAL_MEMORY_SIZE = 4624;
    public static final int VM_ADDRESS = 256; // 16 pages, 16 words per page
    private static final int CYCLES = 10;
    private static final Logger LOG = Logger.getLogger(ComputerInterface.class.getName());
    private final Scanner scanner = new Scanner(System.in);

    public ComputerInterface() {
        out.println("=== Computer started ===");
        initializeComponents();
    }

    private static int getRegisterInput(String input) {
        return Integer.parseInt(input.split("=")[1].toUpperCase());
    }

    private void initializeComponents() {
        clearConsole();
        Cpu cpu = new Cpu();
        RealMemory realMemory = new RealMemory(REAL_MEMORY_SIZE);
        PaginationTable paginationTable = new PaginationTable(realMemory);
        MemoryManager memoryManager = new MemoryManager(cpu, realMemory, paginationTable);
        RealMachine realMachine = new RealMachine(realMemory, cpu, memoryManager, paginationTable);

        commandLoop(realMachine);
    }

    private void commandLoop(RealMachine realMachine) {
        int debug = 0;
        try {
            while (true) {
                displayMenu();
                String command = getLine();
                switch (command) {
                    case "load" -> {
                        out.print("Enter file name: ");
                        String fileName = getLine();
                        handleLoad(realMachine, fileName);
                    }
                    case "clear" -> {
                        out.print("Enter VM number to clear: ");
                        int vmNumber = Integer.parseInt(scanner.nextLine());
                        handleClear(realMachine, vmNumber);
                    }
                    case "run" -> {
                        out.print("Enter VM number to run: ");
                        int runVmNumber = Integer.parseInt(scanner.nextLine());
                        handleRun(realMachine, debug, runVmNumber);
                    }
                    case "debug" -> {
                        out.print("Enter debug mode (1 for on, 0 for off): ");
                        debug = Integer.parseInt(scanner.nextLine());
                        handleDebug(realMachine, debug);
                    }
                    case "super" -> {
                        out.println("Mode set to SUPERVISOR");
                        realMachine.getCpu().setModeEnum(ModeEnum.SUPERVISOR);
                        handleSuper(realMachine);
                    }
                    case "super_run" -> {
                        out.println("Running in SUPERVISOR mode.");
                        realMachine.getCpu().setModeEnum(ModeEnum.SUPERVISOR);
                        realMachine.runSuper();
                    }
                    case "stop" -> realMachine.virtualMachineInterrupt();
                    case "memory" -> showMemoryTable(realMachine);
                    case "cls" -> clearConsole();
                    case "exit" -> {
                        return;
                    }
                    default -> out.println("Invalid command. Please try again.");
                }
            }
        } catch (Exception e) {
            LOG.severe("Error: " + e.getMessage());
        }
    }

    private void handleSuper(RealMachine realMachine) {
        CodeInterpreter codeInterpreter = new CodeInterpreter();
        Stream.iterate(scanner.nextLine(), command -> !"exit".equalsIgnoreCase(command), command -> scanner.nextLine())
                .filter(command -> !command.isEmpty())
                .forEachOrdered(command -> {
                    if (handleRegisterSet(realMachine, command) == 0) {
                        codeInterpreter.loadCommand(realMachine.getMemoryManager(), command, realMachine.getCpu());
                        realMachine.continueRun(realMachine.getCpu().getPtr());
                    }
                });
        out.println("MOVE super mode to user mode");
        realMachine.getCpu().setModeEnum(ModeEnum.USER);
    }

    private void displayMenu() {
        out.printf("%nCommands:%n" +
                "load - Load a program%n" +
                "clear - Clear a virtual machine%n" +
                "run - Run a virtual machine%n" +
                "stop - Stop a virtual machine%n" +
                "super - Enter super mode%n" +
                "super_run - Run in super mode%n" +
                "debug - Toggle debug mode%n" +
                "memory - Display memory tables%n" +
                "cls - Clear the console%n" +
                "exit - Exit the interface%n" +
                "Enter a command: ");
    }

    private void handleRun(RealMachine realMachine, int debug, int vmNumber) {
        if (vmNumber == -1) {
            out.println("Running in SUPERVISOR mode.");
            realMachine.runSuper();
            return;
        }
        if (vmNumber < 0 || vmNumber > 15) {
            out.println("Invalid VM number. Please enter a number between 0 and 15.");
            return;
        }
        if (realMachine.getCpu().getModeEnum() == null) {
            out.println("No program loaded. Use 'load' command to load a program.");
            return;
        }
        if (!realMachine.vmExists(vmNumber)) {
            out.println("VM number " + vmNumber + " does not exist.");
            return;
        }

        if (debug == 1) {
            debugRun(realMachine, vmNumber);
        } else {
            realMachine.run(vmNumber, CYCLES);
        }
    }

    private void handleClear(RealMachine realMachine, int vmNumber) {
        realMachine.clear(vmNumber);
    }

    private void handleLoad(RealMachine realMachine, String fileName) {
        if (fileName.isEmpty()) {
            out.println("Invalid file name. Please try again.");
            return;
        }
        realMachine.load(fileName);
    }

    private String getLine() {
        return scanner.nextLine().replace(" ", "").toLowerCase();
    }

    private void debugRun(RealMachine realMachine, int vmNumber) {
        out.println("Press any key to continue the program. Type 'exit' to exit the program.");
        String input = scanner.nextLine();
        realMachine.preRun(vmNumber);

        while (!"exit".equalsIgnoreCase(input)) {
            long command = realMachine.continueRun(vmNumber);

            out.println("Command: " + CodeEnum.byCode(command) +
                    " AR: " + realMachine.getCpu().getAr() +
                    " BR: " + realMachine.getCpu().getBr() +
                    " ATM: " + realMachine.getCpu().getAtm() +
                    " PTR: " + realMachine.getCpu().getPtr() +
                    " TF: " + realMachine.getCpu().getTf() +
                    " Mode: " + realMachine.getCpu().getModeEnum() +
                    " Exc: " + realMachine.getCpu().getExc() + "\n");

            input = scanner.nextLine();

            handleRegisterSet(realMachine, input);
        }
    }

    private int handleRegisterSet(RealMachine realMachine, String input) {
        if (!input.contains("=") || input.split("=").length != 2) {
            return 0;
        }
        if (input.contains("AR=")) {
            realMachine.getCpu().setPtr(getRegisterInput(input));
            return 1;
        } else if (input.contains("BR=")) {
            realMachine.getCpu().setBr(getRegisterInput(input));
            return 1;
        } else if (input.contains("ATM=")) {
            realMachine.getCpu().setAtm(getRegisterInput(input));
            realMachine.getCpu().setCs(getRegisterInput(input));
            return 1;
        } else if (input.contains("CS=")) {
            realMachine.getCpu().setCs(getRegisterInput(input));
            return 1;
        } else if (input.contains("TF=")) {
            realMachine.getCpu().setTf(getRegisterInput(input));
            return 1;
        } else if (input.contains("Mode=")) {
            realMachine.getCpu().setModeEnum(ModeEnum.valueOf(input));
            return 1;
        } else if (input.contains("Exc=")) {
            realMachine.getCpu().setExc(getRegisterInput(input));
            return 1;
        }
        return 0;
    }

    private void handleDebug(RealMachine realMachine, int debug) {
        if (debug == 1) {
            out.println("Debug mode started.");
        } else {
            out.println("Debug mode stopped.");
            realMachine.virtualMachineInterrupt();
        }
    }

    private void showMemoryTable(RealMachine realMachine) {
        String input;
        do {
            out.printf("%nChoose an option:%n" +
                    "1. Pagination Table%n" +
                    "2. Virtual Machines%n" +
                    "3. Virtual Memory%n" +
                    "4. Full Memory%n" +
                    "5. Registers%n" +
                    "0. Exit%n" +
                    "Enter the number of the option: ");
            input = scanner.nextLine();
            MemoryVisualiser memoryVisualiser = new MemoryVisualiser(realMachine.getRealMemory().getMemory());
            CpuVisualiser cpuVisualiser = new CpuVisualiser(realMachine.getCpu());
            switch (input) {
                case "1" -> memoryVisualiser.showPagination();
                case "2" -> memoryVisualiser.showVirtualMachines();
                case "3" -> memoryVisualiser.showVirtualMemory();
                case "4" -> memoryVisualiser.showFullMemory();
                case "5" -> cpuVisualiser.showRegisters();
                case "0" -> {
                }
                default -> out.println("Invalid input. Please enter a number between 0 and 5.");
            }
        } while (!"0".equals(input));
    }

    private void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Linux, Unix, Mac OS X
                out.print("\033\143");
            }
        } catch (IOException | InterruptedException e) {
            out.println("Error clearing console: " + e.getMessage());
        }
    }
}
