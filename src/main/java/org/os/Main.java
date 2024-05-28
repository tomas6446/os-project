package org.os;

import org.os.core.*;
import org.os.userland.InteractiveInterface;
import org.os.userland.Os;
import org.os.util.MemoryVisualiser;

import static org.os.userland.InteractiveInterface.REAL_MEMORY_SIZE;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Main <interactive|multi_os>");
            return;
        }

        Cpu cpu = new Cpu();
        RealMemory realMemory = new RealMemory(REAL_MEMORY_SIZE);
        SupervisorMemory supervisorMemory = new SupervisorMemory(REAL_MEMORY_SIZE);
        PaginationTable paginationTable = new PaginationTable(realMemory);
        MemoryManager memoryManager = new MemoryManager(cpu, realMemory, paginationTable);
        RealMachine realMachine = new RealMachine(realMemory, cpu, memoryManager, paginationTable, supervisorMemory);
        new MemoryVisualiser(realMachine);

        switch (args[0]) {
            case "interactive" -> new InteractiveInterface(realMachine);
            case "multi_os" -> new Os(realMachine);
            default -> System.out.println("Invalid argument. Usage: java Main <interactive|multi_os>");
        }
    }
}
