package org.os.core;
import com.jakewharton.fliptables.FlipTable;
import lombok.Getter;

import java.util.Arrays;
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
    public void write(int address, int value) {
        memory[address].fromInt(value);
    }

    /*
     * This method is used to allocate a block of memory of size 16 if available
     */
    public int allocate() throws Exception {
        final int blockSize = 16;
        final int realMemoryStart = 16 * 16 + 16;
        int index = 18;
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

    @Override
    public void show() {
        String[] headers = new String[]{"Address", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16"};
        String[][] data = new String[273][17];

        for (int i = 0; i < 273; i++) {
            data[i][0] = String.format("%03d", i+1);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i][j+1] = memory[address].getWord().toString();
            }
        }

        System.out.println("Pagination Table:");
        System.out.println(FlipTable.of(headers, Arrays.copyOfRange(data, 0, 16)));

        System.out.println("Virtual Machines:");
        System.out.println(FlipTable.of(headers, Arrays.copyOfRange(data, 16, 17)));

        System.out.println("Virtual Memory:");
        System.out.println(FlipTable.of(headers, Arrays.copyOfRange(data, 17, 273)));
    }
}
