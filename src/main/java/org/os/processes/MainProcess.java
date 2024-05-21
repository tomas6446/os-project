package org.os.processes;

import java.util.Scanner;

public class MainProcess {
    private final JobGovernor jobGovernor;
    private final Scanner scanner;

    public MainProcess(JobGovernor jobGovernor) {
        scanner = new Scanner(System.in);
        this.jobGovernor = jobGovernor;
    }

    public Packet interact(Packet packet) {
        if (packet != Packet.WORK_END_U_OFF &&
                packet != Packet.WORK_END_U_ADD &&
                packet != Packet.WORK_END_U_START) {
            return packet;
        }

        if (packet == Packet.WORK_END_U_OFF) {
            return Packet.WORK_END;
        }
        return jobGovernor.interact(packet);
    }
}
