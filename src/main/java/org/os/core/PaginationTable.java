package org.os.core;

public class PaginationTable {
    private final RealMemory realMemory;

    public PaginationTable(RealMemory realMemory) {
        this.realMemory = realMemory;
    }

    /*
     * Get a page from the real memory
     */
    public Word get(int index, int ptr) {
        if (realMemory.getMemory()[index].isFree()) {
            allocate(ptr);
            return realMemory.getMemory()[index];
        }
        return realMemory.getMemory()[index];
    }

    /*
     * Allocate a page in the real memory
     */
    public void allocate(int ptr) {
        try {
            int index = realMemory.allocate();
            for (int i = 0; i < 16; i++) {
                if (realMemory.getMemory()[ptr * 16 + i].isFree()) {
                    realMemory.getMemory()[ptr * 16 + i].setRight(index);
                    return;
                }
            }
            throw new RuntimeException("No free slot available in the specified segment");
        } catch (Exception e) {
            throw new RuntimeException("Allocation failed: " + e.getMessage(), e);
        }
    }

    /*
     * Frees the 16 words of memory in pagination table and the real memory
     */
    public void free(int ptr) {
        realMemory.getMemory()[ptr + 16 * 16] = new Word();
        for (int i = 0; i < 16; i++) {
            int index = realMemory.read(ptr + i).getRight();
            realMemory.getMemory()[ptr + i] = new Word();
            realMemory.free(index);
        }
    }
}
