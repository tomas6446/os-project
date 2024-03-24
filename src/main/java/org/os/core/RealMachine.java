package org.os.core;

import lombok.Getter;
import org.os.vm.VirtualMachine;

import java.io.File;

import static java.lang.System.out;
import static org.os.userland.ComputerInterface.VM_ADDRESS;

@Getter
public class RealMachine {
    private final MemoryManager memoryManager;
    private final Cpu cpu;
    private final RealMemory realMemory;
    private final PaginationTable paginationTable;

    public RealMachine(RealMemory realMemory, Cpu cpu, MemoryManager memoryManager, PaginationTable paginationTable) {
        this.realMemory = realMemory;
        this.cpu = cpu;
        this.memoryManager = memoryManager;
        this.paginationTable = paginationTable;
    }

    public boolean load(String programName) {
        out.println("Loading the program " + programName);

        CodeInterpreter codeInterpreter = new CodeInterpreter();
        VirtualMachine virtualMachine = createVM();
        cpu.setModeEnum(ModeEnum.USER);

        File file = new File(programName);
        codeInterpreter.load(memoryManager, file, virtualMachine);

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
        out.println("Stopping the program at index " + index);
        return true;
    }

    public boolean run(int index) {
        out.println("Running the program at index " + index);
        return true;
    }

    public boolean next() {
        out.println("Running the next program");
        return true;
    }

    public boolean interrupt() {
        out.println("Interrupting the current program");
        return true;
    }
}
