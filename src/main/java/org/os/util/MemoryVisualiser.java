package org.os.util;

import com.jakewharton.fliptables.FlipTable;
import org.os.core.Word;

public class MemoryVisualiser {
    private static final String[] headers = new String[]{"Address", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16"};
    private final Word[] memory;

    public MemoryVisualiser(Word[] memory) {
        this.memory = memory;
    }

    public void showPagination() {
        String[][] data = new String[16][17];

        for (int i = 0; i < 16; i++) {
            data[i][0] = String.format("%03d", i + 1);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i][j + 1] = memory[address].getWord().toString();
            }
        }

        System.out.println("Pagination Table:");
        System.out.println(FlipTable.of(headers, data));
    }

    public void showVirtualMachines() {
        String[][] data = new String[1][17];

        data[0][0] = "017";
        for (int j = 0; j < 16; j++) {
            data[0][j + 1] = memory[17].getWord().toString();
        }

        System.out.println("Virtual Machines:");
        System.out.println(FlipTable.of(headers, data));
    }

    public void showVirtualMemory() {
        String[][] data = new String[256][17];

        for (int i = 17; i < 273; i++) {
            data[i - 17][0] = String.format("%03d", i + 1);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i - 17][j + 1] = memory[address].getWord().toString();
            }
        }

        System.out.println("Virtual Memory:");
        System.out.println(FlipTable.of(headers, data));
    }
}
