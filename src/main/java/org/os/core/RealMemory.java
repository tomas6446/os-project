package org.os.core;

import lombok.Getter;

import java.util.stream.IntStream;

@Getter
public class RealMemory implements Memory {
    private final int size;
    private final Word[] memory;

    public RealMemory(int size) {
        this.size = size;
        this.memory = new Word[size];
        IntStream.range(0, size).forEachOrdered(i -> memory[i] = new Word());
    }

    @Override
    public Word read(int address) {
        return memory[address].getWord();
    }

    @Override
    public void write(int address, long value) {
        memory[address].fromInt(value);
    }

    @Override
    public void writeLower(int address, int value) {
        memory[address].setLower(value);
    }

    @Override
    public long readLower(int address) {
        return memory[address].getLower();
    }

    /*
     * This method is used to allocate a block of memory of size 16 if available
     */
    public int allocate() throws Exception {
        final int blockSize = 16;
        final int realMemoryStart = 16 * 16 + 16;
        int index = 17;
        for (int i = realMemoryStart; i < size; i += blockSize, index++) {
            boolean blockIsFree = true;
            for (int j = 0; j < blockSize; j++) {
                if (!memory[i + j].isFree()) {
                    blockIsFree = false;
                    break;
                }
            }
            if (blockIsFree) {
                return index;
            }
        }
        throw new Exception("No free block of size " + blockSize + " is available.");
    }

    public void free(int index) {
        int address = index * 16;
        for (int j = 0; j < 16; j++) {
            memory[address + j] = new Word();
        }
    }
}
