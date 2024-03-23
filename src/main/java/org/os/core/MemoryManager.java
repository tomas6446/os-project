package org.os.core;

import static org.os.core.RealMachine.REAL_MEMORY_SIZE;

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
                if (realAddress > REAL_MEMORY_SIZE) {
                    throw new RuntimeException("Out of memory");
                }
                return realAddress;
            default:
                throw new RuntimeException("Unknown mode");
        }
    }

    public int read(int address, int ptr) {
        int realAddress = getAddress(address, ptr);
        Word word = memory.read(realAddress);
        return word.toInt();
    }

    public void write(int address, int value, int ptr) {
        int realAddress = getAddress(address, ptr);
        memory.write(realAddress, value);
        memory.show();
    }

    /*
        * Convert the virtual address to the real address
        * first 256 is reserved for pagination table
        * next 16 : 257-273 is reserved for VM
        * from 373-4368 real memory is available
     */
    private int toRealAddress(int address, int ptr) {
        return paginationTable.get(ptr + address / 16, ptr).getUpper() * 16 + address % 16 + 16;
    }
}
