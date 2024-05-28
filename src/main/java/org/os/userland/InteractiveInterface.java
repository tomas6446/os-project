package org.os.userland;

import org.os.core.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.lang.System.out;

public class InteractiveInterface {
    public static final int REAL_MEMORY_SIZE = 4624;
    public static final int VM_ADDRESS = 256; // 16 pages, 16 words per page
    private static final int CYCLES = 10;
    private static final Logger LOG = Logger.getLogger(InteractiveInterface.class.getName());
    private final Scanner scanner = new Scanner(System.in);

    public InteractiveInterface(RealMachine realMachine) {
        out.println("=== Computer started ===");
        clearConsole();
        commandLoop(realMachine);
    }

    private static int getRegisterInput(String input) {
        return Integer.parseInt(input.split("=")[1].toUpperCase());
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
                        realMachine.cpu().setModeEnum(ModeEnum.SUPERVISOR);
                        handleSuper(realMachine, true);

                        out.println("MOVE super mode to user mode");
                        realMachine.cpu().setModeEnum(ModeEnum.USER);
                    }
                    case "super_run" -> {
                        out.println("Running in SUPERVISOR mode.");
                        realMachine.cpu().setModeEnum(ModeEnum.SUPERVISOR);
                        handleSuper(realMachine, false);

                        out.print("TI: ");
                        int cycles = Integer.parseInt(scanner.nextLine());
                        realMachine.runSuper(cycles);

                        out.println("MOVE super mode to user mode");
                        realMachine.cpu().setModeEnum(ModeEnum.USER);
                    }
                    case "stop" -> realMachine.virtualMachineInterrupt();
                    case "cls" -> clearConsole();
                    case "exit" -> {
                        return;
                    }
                    default -> out.println("Invalid command. Please try again.");
                }
            }
        } catch (Exception e) {
            LOG.severe("Error: " + e.getMessage());
            commandLoop(realMachine);
        } finally {
            scanner.close();
        }
    }

    private void handleSuper(RealMachine realMachine, boolean runByLine) {
        CodeInterpreter codeInterpreter = new CodeInterpreter();
        Stream.iterate(scanner.nextLine(), command -> !"exit".equalsIgnoreCase(command), command -> scanner.nextLine())
                .filter(command -> !command.isEmpty())
                .forEachOrdered(command -> {
                    if (handleException(realMachine, command) == 0 && handleRegisterSet(realMachine, command) == 0) {
                        codeInterpreter.loadCommand(realMachine.memoryManager(), command, realMachine.cpu());
                    }
                    if (runByLine) {
                        realMachine.runSuper(1);
                    }
                });
        if (!runByLine) {
            realMachine.runSuper(1);
        }
    }

    private int handleException(RealMachine realMachine, String line) {
        if (!line.contains(" ") || line.split(" ").length != 2) {
            return 0;
        }
        CodeEnum command = CodeEnum.valueOf(line.split(" ")[0].toUpperCase());
        if (command == CodeEnum.DIV_ZERO || command == CodeEnum.OVERFLOW || command == CodeEnum.OUT_OF_MEMORY) {
            realMachine.handleCommand(command.getCode());
            realMachine.virtualMachineInterrupt();
            return 1;
        }
        return 0;
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
                "cls - Clear the console%n" +
                "exit - Exit the interface%n" +
                "Enter a command: ");
    }

    private void handleRun(RealMachine realMachine, int debug, int vmNumber) {
        if (vmNumber < 0 || vmNumber > 15) {
            out.println("Invalid VM number. Please enter a number between 0 and 15.");
            return;
        }
        if (realMachine.cpu().getModeEnum() == null) {
            out.println("No program loaded. Use 'load' command to load a program.");
            return;
        }
        if (!realMachine.vmExists(vmNumber)) {
            out.println("VM number " + vmNumber + " does not exist.");
            return;
        }
        realMachine.cpu().setModeEnum(ModeEnum.USER);

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
            realMachine.continueRun(vmNumber);
            input = scanner.nextLine();
            handleRegisterSet(realMachine, input);
        }
    }

    private int handleRegisterSet(RealMachine realMachine, String input) {
        if (!input.contains("=") || input.split("=").length != 2) {
            return 0;
        }
        if (input.contains("AR=")) {
            realMachine.cpu().setPtr(getRegisterInput(input));
            return 1;
        } else if (input.contains("BR=")) {
            realMachine.cpu().setBr(getRegisterInput(input));
            return 1;
        } else if (input.contains("ATM=")) {
            realMachine.cpu().setAtm(getRegisterInput(input));
            realMachine.cpu().setCs(getRegisterInput(input));
            return 1;
        } else if (input.contains("CS=")) {
            realMachine.cpu().setCs(getRegisterInput(input));
            return 1;
        } else if (input.contains("TF=")) {
            realMachine.cpu().setTf(getRegisterInput(input));
            return 1;
        } else if (input.contains("TI=")) {
            realMachine.cpu().setTi(getRegisterInput(input));
            return 1;
        } else if (input.contains("Mode=")) {
            realMachine.cpu().setModeEnum(ModeEnum.valueOf(input));
            return 1;
        } else if (input.contains("Exc=")) {
            realMachine.cpu().setExc(getRegisterInput(input));
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
