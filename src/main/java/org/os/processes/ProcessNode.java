package org.os.processes;

import lombok.Data;
import org.os.core.ExceptionEnum;
import org.os.core.ModeEnum;
import org.os.core.RealMachine;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.stream.IntStream;

@Data
public class ProcessNode {
    private final ProcessEnum processName;
    private Queue<Packet> packets = new LinkedList<>();
    private boolean isWaiting;
    private boolean isInitialized;

    public ProcessNode(ProcessEnum processName) {
        this.processName = processName;
    }

    private static void line() {
        System.out.format("+-------------------------------------+" + "-------------------------------------+" + "-------------------------------------+%n");
    }

    public void addPacket(Packet packet) {
        packets.add(packet);
    }

    public void processPacket(ResourceManager resourceManager) {
        if (!packets.isEmpty()) {
            Packet packet = packets.peek();
            printPacketLog(packet);
            handlePacket(packet, resourceManager);
        }
    }

    private void handlePacket(Packet packet, ResourceManager resourceManager) {
        switch (packet.getTo()) {
            case MAIN_PROC -> handleMainProc(packet, resourceManager);
            case ENVIRONMENT_INTERACTION -> handleEnvironmentInteraction(packet, resourceManager);
            case INTERRUPT -> handleInterrupt(resourceManager);
            case JOB_GOVERNOR -> handleJobGovernor(packet, resourceManager);
            case JCL -> handleJcl(resourceManager);
            case GET_PUT_DATA -> handleGetPutData(packet, resourceManager);
            case VM -> handleVm(packet, resourceManager);
            default -> throw new IllegalStateException("Unexpected value: " + packet.getTo());
        }

    }

    private void handleMainProc(Packet packet, ResourceManager resourceManager) {
        switch (packet.getData()) {
            case "off" -> resourceManager.addPacket(PacketTypeEnum.WORK_END, ProcessEnum.START_STOP, "-");
            case "add", "start" -> resourceManager.addProcess(ProcessEnum.JOB_GOVERNOR);
        }
    }

    private void handleEnvironmentInteraction(Packet packet, ResourceManager resourceManager) {
        String data = packet.getData();
        if (packet.getPacketType() == PacketTypeEnum.INPUT_I) {
            switch (data) {
                case "off" -> resourceManager.addPacket(PacketTypeEnum.WORK_END_U, ProcessEnum.MAIN_PROC, "off");
                case "add" -> resourceManager.addPacket(PacketTypeEnum.WORK_END_U, ProcessEnum.MAIN_PROC, "add");
                case "start" -> resourceManager.addPacket(PacketTypeEnum.WORK_END_U, ProcessEnum.MAIN_PROC, "start");
                default -> resourceManager.addPacket(PacketTypeEnum.INPUT, ProcessEnum.ENVIRONMENT_INTERACTION, "-");
            }
        } else if (packet.getPacketType() == PacketTypeEnum.OUTPUT) {
            System.out.println(data);
        }
    }

    private void handleVm(Packet packet, ResourceManager resourceManager) {
        if (packet.getPacketType() != PacketTypeEnum.RUN_VM) {
            return;
        }

        int vmId = Integer.parseInt(packet.getData());
        resourceManager.getRealMachine().getCpu().setModeEnum(ModeEnum.USER);
        resourceManager.getRealMachine().continueRun(vmId);
        int exc = resourceManager.getRealMachine().getCpu().getExc();
        if (exc == ExceptionEnum.OUTPUT.getValue()) {
            resourceManager.addPacket(PacketTypeEnum.OUTPUT, ProcessEnum.ENVIRONMENT_INTERACTION, "-");
        } else if (exc == ExceptionEnum.INPUT.getValue()) {
            resourceManager.addPacket(PacketTypeEnum.INPUT_I, ProcessEnum.ENVIRONMENT_INTERACTION, "-");
        }
        resourceManager.getRealMachine().getCpu().setModeEnum(ModeEnum.SUPERVISOR);
    }

    private void handleInterrupt(ResourceManager resourceManager) {
        int exc = resourceManager.getRealMachine().getCpu().getExc();
        resourceManager.getRealMachine().handleException();
        resourceManager.addPacket(PacketTypeEnum.ALL_DONE, ProcessEnum.JCL, "Exception thrown: " + ExceptionEnum.byValue(exc));
    }

    private void handleJobGovernor(Packet packet, ResourceManager resourceManager) {
        if (packet.getPacketType() == PacketTypeEnum.RUN_COMPLETE) {
            resourceManager.addPacket(PacketTypeEnum.NEW_VM_TO_LIST, ProcessEnum.JCL, "-");
        }
        resourceManager.addProcess(ProcessEnum.GET_PUT_DATA);
    }

    private void handleJcl(ResourceManager resourceManager) {
        RealMachine realMachine = resourceManager.getRealMachine();
        IntStream.range(0, 15).filter(realMachine::vmExists).forEachOrdered(i -> {
            resourceManager.addProcess(ProcessEnum.VM);
            resourceManager.addPacket(PacketTypeEnum.RUN_VM, ProcessEnum.VM, String.valueOf(i));
        });
    }

    private void handleGetPutData(Packet packet, ResourceManager resourceManager) {
        System.out.println("Enter path for program:");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();

        resourceManager.getRealMachine().load(path);
        resourceManager.addPacket(PacketTypeEnum.RUN_COMPLETE, ProcessEnum.JOB_GOVERNOR, "-");
    }

    private void printPacketLog(Packet packet) {
        System.out.format("| %-35s | %-35s | %-35s |%n", packet.getTo(), packet.getPacketType(), packet.getData());
        line();
    }
}
