package org.os.util;

import com.jakewharton.fliptables.FlipTable;
import lombok.RequiredArgsConstructor;
import org.os.core.Cpu;


@RequiredArgsConstructor
public class CpuVisualiser {
    private static final String[] registers = new String[]{"AR", "BR", "IC", "TF", "PTR", "CS", "ATM", "FLAG", "EXC", "TI", "PI", "MODE"};
    private final Cpu cpu;

    public void showRegisters() {
        String[][] data = new String[registers.length][2];

        long[] registerValues = {cpu.getAr(), cpu.getBr(), cpu.getIc(), cpu.getTf(), cpu.getPtr(), cpu.getCs(), cpu.getAtm(), cpu.getFlag(), cpu.getExc(), cpu.getTi(), cpu.getPi(), cpu.getMode()};
        for (int i = 0; i < registers.length; i++) {
            data[i][0] = registers[i];
            data[i][1] = String.valueOf(registerValues[i]);
        }

        String[] headers = {"Register", "Value"};
        System.out.println("Registers:");
        System.out.println(FlipTable.of(headers, data));
    }
}
