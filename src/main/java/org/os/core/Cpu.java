package org.os.core;

import lombok.Data;

@Data
public class Cpu {
    private CommandEnum commandEnum;
    private ModeEnum modeEnum;
}
