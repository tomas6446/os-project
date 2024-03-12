package org.os.core;

import lombok.Data;

@Data
public class Cpu {
    private int ar;
    private int br;
    private int ic;
    private int tf;
    private int ptr;
    private int cs;
    private int atm;
    private int flag;
    private int exc;
    private int ti;
    private int pi;
    private int mode;

    private CommandEnum commandEnum;
    private ModeEnum modeEnum;
}
