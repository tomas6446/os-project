package org.os.core;

import com.sun.jdi.VMOutOfMemoryException;

public class PaginationTable {
    private final RealMemory realMemory;

    public PaginationTable(RealMemory realMemory) {
        this.realMemory = realMemory;
    }

    /*
     * Get a page from the real memory
     */
    public Word get(int index, int ptr) {
        if (realMemory.getWords()[index].isFree()) {
            allocate(ptr);
            return realMemory.getWords()[index];
        }
        return realMemory.getWords()[index];
    }

    /*
     * Allocate a page in the real memory
     */
    public void allocate(int ptr) {
        try {
            int index = realMemory.allocate();
            for (int i = 0; i < 16; i++) {
                if (realMemory.getWords()[ptr * 16 + i].isFree()) {
                    realMemory.getWords()[ptr * 16 + i].setRight(index);
                    return;
                }
            }
            throw new VMOutOfMemoryException("No free slot available in the specified segment");
        } catch (Exception e) {
            throw new RuntimeException("Allocation failed: " + e.getMessage(), e);
        }
    }

    /*
     * Frees the 16 words of memory in pagination table and the real memory, as well as vm memory
     */
    public void free(int ptr) {
        realMemory.getWords()[ptr + 256] = new Word();
        for (int i = 0; i < 16; i++) {
            int index = realMemory.read(ptr + i).getRight();
            realMemory.getWords()[ptr + i] = new Word();
            realMemory.free(index);
        }
    }
}
