package org.os.core;

public class PaginationTable {
    private final RealMemory realMemory;
    private final int size;

    public PaginationTable(RealMemory realMemory, int size) {
        this.realMemory = realMemory;
        this.size = size;
    }

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
            final int segmentSize = 16;
            int startIndex = ptr * segmentSize;
            int endIndex = startIndex + segmentSize;
            for (int i = startIndex; i < endIndex && i < size; i++) {
                if (realMemory.getMemory()[ptr * 16 + i].isFree()) {
                    realMemory.getMemory()[ptr * 16 + i].setUpper(index);
                    return;
                }
            }
            throw new RuntimeException("No free slot available in the specified segment");
        } catch (Exception e) {
            throw new RuntimeException("Allocation failed: " + e.getMessage(), e);
        }
    }
}
