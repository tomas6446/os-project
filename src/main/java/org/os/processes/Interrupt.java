package org.os.processes;

import org.os.core.RealMachine;

public class Interrupt {
    private final RealMachine realMachine;

    public Interrupt(RealMachine realMachine) {
        this.realMachine = realMachine;
    }

    public Packet interact(Packet packet) {
        int exception = realMachine.getCpu().getExc();
        if (exception == 0) {
            return Packet.ALL_DONE;
        }

        realMachine.handleException();
        return packet;
    }
}
