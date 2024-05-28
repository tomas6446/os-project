package org.os.core;

import com.sun.jdi.VMOutOfMemoryException;
import lombok.Getter;

public class RealMemory extends Memory {
    public RealMemory(int size) {
        super(size);
    }
    /*
     * This method is used to allocate a block of memory of size 16 if available
     */
    public int allocate() {
        final int blockSize = 16;
        final int realMemoryStart = 16 * 16 + 16;
        int index = 17;
        for (int i = realMemoryStart; i < size; i += blockSize, index++) {
            boolean blockIsFree = true;
            for (int j = 0; j < blockSize; j++) {
                if (!words[i + j].isFree()) {
                    blockIsFree = false;
                    break;
                }
            }
            if (blockIsFree) {
                return index;
            }
        }
        throw new VMOutOfMemoryException("No free block of size " + blockSize + " is available.");
    }

    public void free(int index) {
        int address = index * 16;
        for (int j = 0; j < 16; j++) {
            words[address + j] = new Word();
        }
    }
}
