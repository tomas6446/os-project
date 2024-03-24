package org.os.core;

import org.os.vm.VirtualMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CodeInterpreter {
    public void load(MemoryManager memoryManager, File file, VirtualMachine virtualMachine) {
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                String line;
                int counter = 0;
                while ((line = reader.readLine()) != null) {
                    List<String> args = List.of(line.split(" "));
                    CodeEnum command = CodeEnum.valueOf(args.getFirst());

                    if (command == CodeEnum.MOVE) {
                        List<String> moveArgs = List.of(args.get(1).split(","));
                        long regValue1 = CodeEnum.valueOf(moveArgs.get(0)).getCode();
                        long regValue2 = CodeEnum.valueOf(moveArgs.get(1)).getCode();

                        memoryManager.write(virtualMachine.getPtr() + counter, command.getCode(), virtualMachine.getPtr());
                        memoryManager.write(virtualMachine.getPtr() + counter + 1, regValue1, virtualMachine.getPtr());
                        memoryManager.write(virtualMachine.getPtr() + counter + 2, regValue2, virtualMachine.getPtr());
                        counter += 3;
                        continue;
                    }
                    memoryManager.write(virtualMachine.getPtr() + counter, command.getCode(), virtualMachine.getPtr());
                    counter++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

// ADD
// SUB
// MOVE 1, AR
