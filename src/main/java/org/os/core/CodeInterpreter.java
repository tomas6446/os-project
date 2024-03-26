package org.os.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.function.ToLongFunction;

public class CodeInterpreter {
    public void load(MemoryManager memoryManager, File file, Cpu cpu) {
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int counter = 0;
                memoryManager.write(0, CodeEnum.JM.getCode(), cpu.getPtr()); // jump to the first command
                counter += 2;

                while ((line = reader.readLine()) != null) {
                    List<String> args = List.of(line.split(" "));
                    CodeEnum command = CodeEnum.valueOf(args.getFirst().toUpperCase());
                    switch (command) {
                        case DATA_SEGMENT -> {
                            long valCountToJump = 0;
                            while ((line = reader.readLine()) != null) {
                                CodeEnum dataCommand = CodeEnum.valueOf(line.split(" ")[0].toUpperCase());
                                if (dataCommand == CodeEnum.VAL) {
                                    long data = Long.parseLong(line.split(" ")[1]);
                                    memoryManager.write(counter, data, cpu.getPtr());
                                    counter++;
                                    valCountToJump++;
                                }
                                if (dataCommand == CodeEnum.CODE_SEGMENT) {
                                    break;
                                }
                            }
                            memoryManager.write(1, 2L + valCountToJump, cpu.getPtr());
                        }
                        case CodeEnum.MOVE -> {
                            List<String> moveArgs = List.of(args.get(1).toUpperCase().toUpperCase().split(","));

                            memoryManager.write(counter, command.getCode(), cpu.getPtr());
                            counter++;

                            ToLongFunction<String> getRegValue = (arg) -> isNumber(arg) ? Long.parseLong(arg) : CodeEnum.valueOf(arg).getCode();
                            long value1 = getRegValue.applyAsLong(moveArgs.get(0));
                            long value2 = getRegValue.applyAsLong(moveArgs.get(1));

                            memoryManager.write(counter, value1, cpu.getPtr());
                            memoryManager.write(counter + 1, value2, cpu.getPtr());
                            counter += 2;
                        }
                        case CodeEnum.JM, CodeEnum.JG, CodeEnum.JL, CodeEnum.JLR, CodeEnum.JGR, CodeEnum.LD, CodeEnum.ST -> {
                            memoryManager.write(counter, command.getCode(), cpu.getPtr());
                            memoryManager.write(counter + 1, Long.parseLong(args.get(1)), cpu.getPtr());
                            counter += 2;
                        }
                        default -> {
                            memoryManager.write(counter, command.getCode(), cpu.getPtr());
                            counter++;
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isNumber(String s) {
        return s.chars().allMatch(Character::isDigit);
    }
}
