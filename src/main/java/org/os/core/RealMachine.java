package org.os.core;

import org.os.vm.VirtualMachine;

import java.io.File;

public class RealMachine {
    private static final int VM_ADDRESS = 256; // PROC 16 pages, 16 words per page
    public static int REAL_MEMORY_SIZE = 4624;
    public static int PAGINATION_TABLE_SIZE = 256; // 16 pages, 16 words per page (4 bytes per word)
    private final MemoryManager memoryManager;
    private final Cpu cpu;
    private final RealMemory realMemory;
    private final PaginationTable paginationTable;

    public RealMachine() {
        cpu = new Cpu();
        realMemory = new RealMemory(REAL_MEMORY_SIZE);
        paginationTable = new PaginationTable(realMemory, PAGINATION_TABLE_SIZE);
        memoryManager = new MemoryManager(cpu, realMemory, paginationTable);
    }

    public boolean load(String programName) {
        System.out.println("Loading the program " + programName);

        Loader loader = new Loader();
        VirtualMachine virtualMachine = createVM();
        cpu.setModeEnum(ModeEnum.USER);

        File file = new File(programName);
        loader.load(memoryManager, file, virtualMachine);

        return true;
    }

    private VirtualMachine createVM() {
        ModeEnum lastMode = cpu.getModeEnum();
        cpu.setModeEnum(ModeEnum.SUPERVISOR);

        int ptr = 0;
        while (memoryManager.read(VM_ADDRESS + ptr, ptr) == 1) {
            ptr++;
        }

        VirtualMachine virtualMachine = new VirtualMachine(0, 0, 0, 0, 0, ptr);
        paginationTable.allocate(ptr);

        memoryManager.write(VM_ADDRESS + ptr, 1, ptr);
        cpu.setModeEnum(lastMode);
        return virtualMachine;
    }

    public boolean unload(int index) {
        System.out.println("Stopping the program at index " + index);
        return true;
    }

    public boolean run(int index) {
        System.out.println("Running the program at index " + index);
        return true;
    }

    public boolean next() {
        System.out.println("Running the next program");
        return true;
    }

    public boolean interrupt() {
        System.out.println("Interrupting the current program");
        return true;
    }
}
