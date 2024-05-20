package org.os.userland;

import org.os.core.*;
import org.os.processes.StartStop;

import static java.lang.System.out;
import static org.os.userland.InteractiveInterface.REAL_MEMORY_SIZE;

public class Os {
    public Os() {
        out.println("=== Computer started ===");
        initializeComponents();
    }

    private void initializeComponents() {
        Cpu cpu = new Cpu();
        RealMemory realMemory = new RealMemory(REAL_MEMORY_SIZE);
        PaginationTable paginationTable = new PaginationTable(realMemory);
        MemoryManager memoryManager = new MemoryManager(cpu, realMemory, paginationTable);
        RealMachine realMachine = new RealMachine(realMemory, cpu, memoryManager, paginationTable);

        new StartStop(realMachine);
    }
}
