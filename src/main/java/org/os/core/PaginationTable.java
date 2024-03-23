package org.os.core;

import java.util.stream.IntStream;

public class PaginationTable {
    private final RealMemory realMemory;
    private final Word[] table;

    public PaginationTable(RealMemory realMemory, int size) {
        this.realMemory = realMemory;
        this.table = new Word[size];
        IntStream.range(0, size).forEachOrdered(i -> table[i] = new Word());
    }

    public Word get(int index, int ptr) {
        if (table[index].isFree()) {
            allocate(ptr);
            return table[index];
        }
        return table[index];
    }

    /*
     * Allocate a page in the real memory
     */
    public void allocate(int ptr) {
        try {
            int index = realMemory.allocate();
            final int segmentSize = 16;
            int startIndex = (ptr - 1) * segmentSize;
            int endIndex = startIndex + segmentSize;
            for (int i = startIndex; i < endIndex && i < table.length; i++) {
                if (table[i].isFree()) {
                    table[i].setUpper(index);
                    return;
                }
            }
            throw new RuntimeException("No free slot available in the specified segment");
        } catch (Exception e) {
            throw new RuntimeException("Allocation failed: " + e.getMessage(), e);
        }
    }
}
