package org.os.processes;

public class JobGovernor {
    private final GetPutData getPutData;

    public JobGovernor(GetPutData getPutData) {
        this.getPutData = getPutData;
    }

    public Packet interact(Packet packet) {
        /* Procesas blokuojamas, kol patikrinama prieiga prie atminties */
        /* Procesas blokuojamas, kol atsilaisvins atmintis */
        packet = getPutData.interact(packet);
        if (packet != Packet.RUN_COMPLETE) {
            return packet;
        }
        return Packet.NEW_VM_TO_LIST;
    }
}
