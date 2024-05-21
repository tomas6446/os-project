package org.os.processes;

import org.os.core.RealMachine;

public class GetPutData {
    private final RealMachine realMachine;

    public GetPutData(RealMachine realMachine) {
        this.realMachine = realMachine;
    }

    public Packet interact(Packet packet) {
        String path = packet.getData();
        realMachine.load(path);
        return Packet.RUN_COMPLETE;
    }
}
