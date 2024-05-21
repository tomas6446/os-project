package org.os.processes;

import org.os.core.ExceptionEnum;
import org.os.core.ModeEnum;
import org.os.core.RealMachine;

public class Vm {
    private final RealMachine realMachine;

    public Vm(RealMachine realMachine) {
        this.realMachine = realMachine;
    }

    public Packet interact(Packet packet, int vmId) {
        realMachine.getCpu().setModeEnum(ModeEnum.USER);
        realMachine.continueRun(vmId);

        if (realMachine.getCpu().getExc() == ExceptionEnum.INPUT.getValue()) {
            realMachine.getCpu().setModeEnum(ModeEnum.SUPERVISOR);
            if (packet == Packet.INPUT) {
                return packet;
            }
        } else if (realMachine.getCpu().getExc() == ExceptionEnum.OUTPUT.getValue()) {
            realMachine.getCpu().setModeEnum(ModeEnum.SUPERVISOR);
            return Packet.OUTPUT;
        }
        realMachine.getCpu().setModeEnum(ModeEnum.USER);
        return packet;
    }
}
