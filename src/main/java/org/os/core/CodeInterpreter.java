package org.os.core;

import org.os.vm.VirtualMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Loader {
    public void load(MemoryManager memoryManager, File file, VirtualMachine virtualMachine) {
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                String line;
                int address = 0;
                while ((line = reader.readLine()) != null) {
                    List<String> args = List.of(line.split(" "));
                    CommandEnum command = CommandEnum.valueOf(args.getFirst());

                    if (command == CommandEnum.MOVE) {
                        List<String> moveArgs = List.of(args.get(1).split(","));
                        int regValue1 = Integer.parseInt(moveArgs.get(0));
                        int regValue2 = Integer.parseInt(moveArgs.get(1));

                        memoryManager.write(virtualMachine.getPtr() + address, command.getCode(), virtualMachine.getPtr());
                        memoryManager.write(virtualMachine.getPtr() + address + 1, regValue1, virtualMachine.getPtr());
                        memoryManager.write(virtualMachine.getPtr() + address + 2, regValue2, virtualMachine.getPtr());
                        address += 3;
                        continue;
                    }
                    memoryManager.write(virtualMachine.getPtr() + address, command.getCode(), virtualMachine.getPtr());
                    address++;
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
