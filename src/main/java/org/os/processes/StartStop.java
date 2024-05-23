package org.os.processes;

import org.os.core.RealMachine;

public class StartStop {
    private final ResourceManager resourceManager;

    public StartStop(RealMachine realMachine) {
        resourceManager = new ResourceManager(realMachine);
        printPacketHeader();
        initializeProcesses();
        process();
    }

    private static void line() {
        System.out.format("+-------------------------------------+" +
                "-------------------------------------+" +
                "-------------------------------------+%n");
    }

    private void initializeProcesses() {
        resourceManager.addProcess(ProcessEnum.JOB_GOVERNOR);
        resourceManager.addProcess(ProcessEnum.ENVIRONMENT_INTERACTION);
        resourceManager.addProcess(ProcessEnum.INTERRUPT);
        resourceManager.addProcess(ProcessEnum.MAIN_PROC);
        resourceManager.addProcess(ProcessEnum.JCL);
    }

    private void process() {
        while (true) {
            for (ProcessNode node : resourceManager.getProcesses().values()) {
                if (!node.getPackets().isEmpty()) {
                    node.processPacket(resourceManager);
                }
            }
        }
    }

    private void printPacketHeader() {
        line();
        System.out.format("| %-35s | %-35s | %-35s |%n", "TO", "TYPE", "DATA");
        line();
    }
}
