package org.os.core;


import com.sun.jdi.VMOutOfMemoryException;
import lombok.Getter;

import static org.os.userland.InteractiveInterface.REAL_MEMORY_SIZE;
import static org.os.userland.InteractiveInterface.VM_ADDRESS;


@Getter
public class MemoryManager {
    private final Cpu cpu;
    private final Memory memory;
    private final PaginationTable paginationTable;

    public MemoryManager(Cpu cpu, Memory memory, PaginationTable paginationTable) {
        this.cpu = cpu;
        this.memory = memory;
        this.paginationTable = paginationTable;
    }

    private int getAddress(int address, int ptr) throws RuntimeException {
        switch (cpu.getModeEnum()) {
            case SUPERVISOR:
                return address;
            case USER:
                int realAddress = toRealAddress(address, ptr);
                if (realAddress > REAL_MEMORY_SIZE || realAddress < VM_ADDRESS + 16) {
                    throw new VMOutOfMemoryException("Out of memory");
                }
                return realAddress;
            default:
                throw new RuntimeException("Unknown mode");
        }
    }

    public long read(int address, int ptr) {
        int realAddress = getAddress(address, ptr);
        Word word = memory.read(realAddress);
        return word.toInt();
    }

    public void write(int address, long value, int ptr) {
        int realAddress = getAddress(address, ptr);
        memory.write(realAddress, value);
    }

    /*
     * Convert the virtual address to the real address
     * first 256 is reserved for pagination table
     * next 16 : 256-273 is reserved for VM
     * from 273-4368 real memory
     */
    public int toRealAddress(int address, int ptr) {
        int pageSize = 16;
        int index = (ptr * 16) + address / pageSize;
        int frame = paginationTable.get(index, ptr).getRight();
        return frame * pageSize + address % pageSize;
    }

    public void free(int ptr) {
        paginationTable.free(ptr);
    }
}
