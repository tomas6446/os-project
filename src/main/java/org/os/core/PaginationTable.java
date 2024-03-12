package org.os.core;

public class PaginationTable {
    private final RealMemory realMemory;
    private final int[] table;

    public PaginationTable(RealMemory realMemory, int size) {
        this.realMemory = realMemory;
        table = new int[size];
    }

    /*
     * Allocate a page in the real memory
     */
    public void allocate(int ptr) {
        try {
            int address = realMemory.allocate();
            final int segmentSize = 16;
            int startIndex = ptr * segmentSize;
            int endIndex = startIndex + segmentSize;
            for (int i = startIndex; i < endIndex && i < table.length; i++) {
                if (table[i] == 0) {
                    table[i] = address;
                    return;
                }
            }
            throw new RuntimeException("No free slot available in the specified segment");
        } catch (Exception e) {
            throw new RuntimeException("Allocation failed: " + e.getMessage(), e);
        }
    }
}
