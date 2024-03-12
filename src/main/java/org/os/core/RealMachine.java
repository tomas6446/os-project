package org.os.core;

import org.os.vm.VirtualMachine;

public class RealMachine {
    private final MemoryManager memoryManager;
    private final Cpu cpu;
    private final RealMemory realMemory;
    private PaginationTable paginationTable;

    private static int VM_ADDRESS = 1024; // PROC 16 pages, 16 words per page, 4 bytes per word
    public static int REAL_MEMORY_WORD_SIZE = 4368; // 682 pages, 16 words per page
    public static int PAGINATION_TABLE = 4368; // 273 pages, 16 words per page
    private static int WORD_SIZE = 4;

    public RealMachine() {
        cpu = new Cpu();
        realMemory = new RealMemory(REAL_MEMORY_WORD_SIZE);
        paginationTable = new PaginationTable(realMemory, PAGINATION_TABLE);
        memoryManager = new MemoryManager(cpu, realMemory);
    }

    public boolean load(String programName) {
        System.out.println("Loading the program " + programName);

        VirtualMachine virtualMachine = createVM();
        cpu.setModeEnum(ModeEnum.USER);
        cpu.setPtr(virtualMachine.getPtr());

        return true;
    }

    private VirtualMachine createVM() {
        ModeEnum lastMode = cpu.getModeEnum();
        cpu.setModeEnum(ModeEnum.SUPERVISOR);

        int ptr = 0;
        while (memoryManager.read(VM_ADDRESS + ptr * WORD_SIZE, ptr) == 1) {
            ptr++;
        }

        VirtualMachine virtualMachine = new VirtualMachine(0, 0, 0, 0, 0, ptr);
        paginationTable.allocate(ptr);

        memoryManager.write(VM_ADDRESS + ptr * WORD_SIZE, 1, ptr);
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
