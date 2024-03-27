package org.os.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToLongFunction;

import static java.lang.System.out;

public class CodeInterpreter {

    private void increaseCounter(Cpu cpu, int value) {
        cpu.setCs(cpu.getCs() + value);
    }

    public void load(MemoryManager memoryManager, File file, Cpu cpu) {
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                memoryManager.write(0, CodeEnum.JM.getCode(), cpu.getPtr()); // jump to the first command
                increaseCounter(cpu, 2);

                while ((line = reader.readLine()) != null) {
                    List<String> args = List.of(line.split(" "));
                    CodeEnum command = CodeEnum.valueOf(args.getFirst().toUpperCase());
                    int counter;
                    counter = switch (command) {
                        case DATA_SEGMENT -> handleSegment(memoryManager, cpu, reader);
                        case CodeEnum.MOVE -> handleMove(memoryManager, cpu, args, command);
                        case CodeEnum.JM, CodeEnum.JG, CodeEnum.JL, CodeEnum.JLR, CodeEnum.JGR, CodeEnum.LD, CodeEnum.ST ->
                                handleOther(memoryManager, command.getCode(), cpu, Long.parseLong(args.get(1)));
                        default -> handleDefault(memoryManager, command.getCode(), cpu);
                    };
                    cpu.setCs(counter);
                }
            }
            out.println("Program loaded. Use 'run' command to start the program");
        } catch (IOException e) {
            out.println("Error while reading file: " + e.getMessage());
        }
    }

    private boolean isNumber(String s) {
        return s.chars().allMatch(Character::isDigit);
    }

    public int loadCommand(MemoryManager memoryManager, String line, Cpu cpu) {
        List<String> args = List.of(line.split(" "));
        CodeEnum command = CodeEnum.valueOf(args.getFirst().toUpperCase());

        return switch (command) {
            case CodeEnum.MOVE -> handleMove(memoryManager, cpu, args, command);
            case CodeEnum.VAL -> handleVal(memoryManager, cpu, args);
            case CodeEnum.JM, CodeEnum.JG, CodeEnum.JL, CodeEnum.JLR, CodeEnum.JGR, CodeEnum.LD, CodeEnum.ST ->
                    handleOther(memoryManager, command.getCode(), cpu, Long.parseLong(args.get(1)));
            default -> handleDefault(memoryManager, command.getCode(), cpu);
        };
    }

    private int handleVal(MemoryManager memoryManager, Cpu cpu, List<String> args) {
        if (args.size() != 2) {
            return cpu.getCs();
        }
        long data = Long.parseLong(args.get(1));
        return handleDefault(memoryManager, data, cpu);
    }

    private int handleDefault(MemoryManager memoryManager, long command, Cpu cpu) {
        memoryManager.write(cpu.getCs(), command, cpu.getPtr());
        increaseCounter(cpu, 1);
        return cpu.getCs();
    }


    private int handleOther(MemoryManager memoryManager, long reg, Cpu cpu, long val) {
        memoryManager.write(cpu.getCs(), reg, cpu.getPtr());
        memoryManager.write(cpu.getCs() + 1, val, cpu.getPtr());
        increaseCounter(cpu, 2);
        return cpu.getCs();
    }

    private int handleSegment(MemoryManager memoryManager, Cpu cpu, BufferedReader reader) throws IOException {
        String line;
        long valCountToJump = 0;
        while ((line = reader.readLine()) != null) {
            CodeEnum dataCommand = CodeEnum.valueOf(line.split(" ")[0].toUpperCase());
            if (dataCommand == CodeEnum.VAL) {
                long data = Long.parseLong(line.split(" ")[1]);
                int counter = handleDefault(memoryManager, data, cpu);
                cpu.setCs(counter);
                valCountToJump++;
            }
            if (dataCommand == CodeEnum.CODE_SEGMENT) {
                break;
            }
        }
        memoryManager.write(1, 2L + valCountToJump, cpu.getPtr());
        return cpu.getCs();
    }

    private int handleMove(MemoryManager memoryManager, Cpu cpu, List<String> args, CodeEnum command) {
        String input = removeBlankStrings(args);

        List<String> moveArgs = Arrays.stream(input.toUpperCase().split(","))
                .map(String::trim)
                .toList();

        int counter = handleDefault(memoryManager, command.getCode(), cpu);
        cpu.setCs(counter);

        ToLongFunction<String> getRegValue = (arg) -> isNumber(arg) ? Long.parseLong(arg) : CodeEnum.valueOf(arg).getCode();
        long value1 = getRegValue.applyAsLong(moveArgs.get(0));
        long value2 = getRegValue.applyAsLong(moveArgs.get(1));

        handleOther(memoryManager, value1, cpu, value2);
        return cpu.getCs();
    }

    private String removeBlankStrings(List<String> args) {
        StringBuilder inputBuilder = new StringBuilder();
        for (int i = 1; i < args.size(); i++) { // Start from index 1 as index 0 is "MOVE"
            String arg = args.get(i);
            if (!arg.trim().isEmpty()) { // Skip empty strings
                inputBuilder.append(arg);
            }
        }
        String input = inputBuilder.toString();
        return input;
    }
}
