package org.os.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.os.core.Cpu;

@Getter
@RequiredArgsConstructor
public class CpuVisualiser {
    private final String[] cpuRegisters = new String[]{"AR", "BR", "TF", "PTR", "CS", "ATM", "FLAG", "EXC", "TI", "PI", "MODE"};
    private final String[] cpuHeaders = new String[]{"Register", "Value"};
    private final Cpu cpu;

    public String[][] getRegisterData() {
        String[][] data = new String[cpuRegisters.length][2];

        long[] registerValues = {
                cpu.getAr(), cpu.getBr(), cpu.getTf(), cpu.getPtr(),
                cpu.getCs(), cpu.getAtm(), cpu.getFlag(), cpu.getExc(),
                cpu.getTi(), cpu.getPi(), cpu.getMode()
        };

        for (int i = 0; i < cpuRegisters.length; i++) {
            data[i][0] = cpuRegisters[i]; // Register name
            data[i][1] = String.valueOf(registerValues[i]); // Register value
        }

        return data;
    }
}
