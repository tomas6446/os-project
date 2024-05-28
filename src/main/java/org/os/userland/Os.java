package org.os.userland;

import org.os.core.*;
import org.os.proc.Planner;
import org.os.proc.ResourceManager;
import org.os.util.MemoryVisualiser;

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
        SupervisorMemory supervisorMemory = new SupervisorMemory(REAL_MEMORY_SIZE);
        PaginationTable paginationTable = new PaginationTable(realMemory);
        MemoryManager memoryManager = new MemoryManager(cpu, realMemory, paginationTable);
        RealMachine realMachine = new RealMachine(realMemory, cpu, memoryManager, paginationTable, supervisorMemory);

        ResourceManager resourceManager = new ResourceManager();
        new Planner(realMachine, resourceManager).plan();
    }
}
