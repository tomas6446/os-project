package org.os.vm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VirtualMachine {
    private int ar;
    private int br;
    private int atm;
    private int ic;
    private int tf;
    private int ptr;

    public int getRegisterValue(String register) {
        return switch (register) {
            case "AR" -> ar;
            case "BR" -> br;
            case "ATM" -> atm;
            case "IC" -> ic;
            case "TF" -> tf;
            case "PTR" -> ptr;
            default -> throw new IllegalArgumentException("Unknown register: " + register);
        };
    }
}
