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

}
