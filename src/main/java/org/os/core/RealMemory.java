package org.os.core;

import java.util.stream.IntStream;

public class RealMemory implements Memory {
    private int size;
    private Word[] memory;

    public RealMemory(int size) {
        this.size = size;
        memory = new Word[size];
        IntStream.range(0, size)
                .forEachOrdered(i -> memory[i] = new Word());
    }

    @Override
    public Word read(int address) {
        return memory[address].getWord();
    }

    @Override
    public void write(int address, int value) {
        memory[address].fromInt(value);
    }

    /*
    * This method is used to allocate a block of memory of size 16.
     */
    public int allocate() throws Exception {
        final int blockSize = 16;
        for (int i = 0; i < size; i += blockSize) {
            boolean blockIsFree = true;
            for (int j = 0; j < blockSize; j++) {
                if (!memory[i + j].isFree()) {
                    blockIsFree = false;
                    break;
                }
            }
            if (blockIsFree) {
                return i;
            }
        }
        throw new Exception("No free block of size " + blockSize + " is available.");
    }
}
