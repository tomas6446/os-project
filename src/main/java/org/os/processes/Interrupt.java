package org.os.processes;

import org.os.core.ExceptionEnum;
import org.os.core.RealMachine;

public class Interrupt {
    private final RealMachine realMachine;

    public Interrupt(RealMachine realMachine) {
        this.realMachine = realMachine;
    }

    public Packet interact(Packet packet) {
        int exception = realMachine.getCpu().getExc();
        if (exception == ExceptionEnum.NO_EXCEPTION.getValue()) {
            return packet;
        }

        realMachine.handleException();
        return Packet.ALL_DONE;
    }
}
