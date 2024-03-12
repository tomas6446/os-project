package org.os.core;

import static org.os.core.RealMachine.REAL_MEMORY_WORD_SIZE;

public class MemoryManager {
    private final Cpu cpu;
    private final Memory memory;

    public MemoryManager(Cpu cpu, Memory memory) {
        this.cpu = cpu;
        this.memory = memory;


    }

    private int getAddress(int address, int ptr) throws RuntimeException {
        switch (cpu.getModeEnum()) {
            case SUPERVISOR:
                return address;
            case USER:
                int realAddress = toRealAddress(address, ptr);
                if (realAddress > REAL_MEMORY_WORD_SIZE) {
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
    }

    private int toRealAddress(int address, int ptr) {
        return ptr * 16 + address;
    }
}
