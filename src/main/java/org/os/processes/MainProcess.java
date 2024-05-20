package org.os.processes;

import org.os.core.RealMachine;

import java.util.Scanner;

public class MainProcess {
    private final JobGovernor jobGovernor;
    private final Scanner scanner;

    public MainProcess(RealMachine realMachine, JobGovernor jobGovernor) {
        scanner = new Scanner(System.in);
        this.jobGovernor = jobGovernor;
    }

    public Packet interact(Packet packet) {
        if (packet != Packet.WORK_END_U_OFF &&
                packet != Packet.WORK_END_U_ADD &&
                packet != Packet.WORK_END_U_START) {
            return packet;
        }

        System.out.println("1. Continue with existing program");
        System.out.println("2. Exit");

        int choice = scanner.nextInt();
        return switch (choice) {
            case 1 -> jobGovernor.interact(packet);
            case 2 -> Packet.WORK_END;
            default -> packet;
        };
    }
}
