package org.os.core;

import lombok.Data;

@Data
public class Cpu {
    private int ar;
    private int br;
    private int tf;
    private int ptr;
    private int cs;
    private int atm;
    private int flag;
    private int exc;
    private int ti;
    private int pi;
    private int mode;

    private ModeEnum modeEnum;

    public String toString() {
        return "CPU Registers:\n" +
                "--------------\n" +
                "AR:    " + ar + "\n" +
                "BR:    " + br + "\n" +
                "ATM:   " + atm + "\n" +
                "PTR:   " + ptr + "\n" +
                "TF:    " + tf + "\n" +
                "CS:    " + cs + "\n" +
                "IT:    " + ti + "\n" +
                "Mode:  " + modeEnum + "\n" +
                "Exc:   " + exc + "\n" +
                "--------------" + "\n";
    }
}
