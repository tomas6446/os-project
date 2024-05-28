package org.os.proc;

import org.os.core.ExceptionEnum;
import org.os.core.ModeEnum;
import org.os.core.RealMachine;
import org.os.util.Logger;
import org.os.util.MemoryVisualiser;

import java.io.IOException;
import java.util.Deque;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Planner {
    private boolean isWorking = true;
    private final ResourceManager resourceManager;
    private final RealMachine realMachine;
    private final Logger logger;
    private final Scanner scanner;
    private MemoryVisualiser memoryVisualiser;

    public Planner(RealMachine realMachine, ResourceManager resourceManager) {
        this.realMachine = realMachine;
        this.resourceManager = resourceManager;
        this.logger = new Logger();
        this.scanner = new Scanner(System.in);
        this.memoryVisualiser = new MemoryVisualiser(realMachine.getRealMemory().getMemory());

        resourceManager.addPacket(ProcessEnum.START_STOP, new Packet(PacketTypeEnum.RUNNING));
    }

    public void plan() {
        while (isWorking) {
            for (ProcessEnum process : ProcessEnum.values()) {
                Deque<Packet> packetQueue = resourceManager.getProcessPackets(process);
                if (!packetQueue.isEmpty()) {
                    Packet packet = (process == ProcessEnum.ENVIRONMENT_INTERACTION ||
                            process == ProcessEnum.JCL ||
                            process == ProcessEnum.INTERRUPT ||
                            process == ProcessEnum.VM) ? packetQueue.peek() : packetQueue.poll();
                    handleProcess(process, packet);
                }
            }
        }
    }

    private void handleProcess(ProcessEnum process, Packet packet) {
        try {
            switch (process) {
                case MAIN_PROC -> handleMainProc(packet);
                case JOB_GOVERNOR -> handleJobGovernor(packet);
                case ENVIRONMENT_INTERACTION -> handleEnvironmentInteraction(packet);
                case INTERRUPT -> handleInterrupt();
                case JCL -> handleJcl();
                case GET_PUT_DATA -> handleGetPutData(packet);
                case START_STOP -> handleStartStop(packet);
                case VM -> handleVm(packet);
            }
        } catch (IOException e) {
            System.out.println("Error while handling packet: " + packet);
        }
    }

    private void handleStartStop(Packet packet) {
        if (packet.getType() == PacketTypeEnum.WORK_END) {
            isWorking = false;
        }

        resourceManager.addPacket(ProcessEnum.ENVIRONMENT_INTERACTION, new Packet(PacketTypeEnum.RUNNING));
        resourceManager.addPacket(ProcessEnum.INTERRUPT, new Packet(PacketTypeEnum.RUNNING));
        resourceManager.addPacket(ProcessEnum.MAIN_PROC, new Packet(PacketTypeEnum.RUNNING));
    }

    private void handleVm(Packet packet) {
        if (packet.getType() != PacketTypeEnum.RUN_VM) {
            return;
        }
        int vmId = Integer.parseInt(packet.getData());

        realMachine.preRun(vmId);
        String output = realMachine.continueRun(vmId);
        realMachine.virtualMachineInterrupt();
        logger.writeOutputToFile(vmId, output);

        ExceptionEnum exception = ExceptionEnum.byValue(realMachine.getCpu().getExc());
        switch (exception) {
            case OUTPUT -> resourceManager.addPacketToFront(ProcessEnum.ENVIRONMENT_INTERACTION, new Packet(PacketTypeEnum.OUTPUT));
            case INPUT -> resourceManager.addPacketToFront(ProcessEnum.ENVIRONMENT_INTERACTION, new Packet(PacketTypeEnum.INPUT_I));
        }
        resourceManager.removePacket(ProcessEnum.VM, packet);
        realMachine.getCpu().setModeEnum(ModeEnum.SUPERVISOR);
    }

    private void handleGetPutData(Packet packet) {
        if (packet.getType() == PacketTypeEnum.OUTPUT) {
            System.out.println(packet.getData());
            return;
        }
        System.out.println("Program path: ");
        String path = scanner.nextLine();
        int vmId = realMachine.load(path);
        logger.writeOutputToFile(vmId, "Program loaded: " + path);
        resourceManager.addPacket(ProcessEnum.JOB_GOVERNOR, new Packet(PacketTypeEnum.RUN_COMPLETE));
    }

    private void handleJcl() {
        IntStream.range(0, 15).filter(realMachine::vmExists).forEachOrdered(i ->
                resourceManager.addPacket(ProcessEnum.VM, new Packet(PacketTypeEnum.RUN_VM, String.valueOf(i))));
    }

    private void handleInterrupt() {
        realMachine.handleException();
        realMachine.getCpu().setExc(ExceptionEnum.NO_EXCEPTION.getValue());
    }

    private void handleEnvironmentInteraction(Packet packet) throws IOException {
        switch (packet.getType()) {
            case INPUT_I, RUNNING -> {
                // Check for user input and process it
                String data = "";
                if (System.in.available() > 0) {
                    data = scanner.nextLine();
                }
                switch (data) {
                    case "off" -> resourceManager.addPacket(ProcessEnum.MAIN_PROC, new Packet(PacketTypeEnum.WORK_END_U, "off"));
                    case "add" -> resourceManager.addPacket(ProcessEnum.MAIN_PROC, new Packet(PacketTypeEnum.WORK_END_U, "add"));
                    case "start" -> resourceManager.addPacket(ProcessEnum.MAIN_PROC, new Packet(PacketTypeEnum.WORK_END_U, "start"));
                }
            }
            case OUTPUT -> {
                System.out.println(realMachine.getSupervisorMemory().print());
                resourceManager.removePacket(ProcessEnum.ENVIRONMENT_INTERACTION, packet);
            }
        }
    }

    private void handleJobGovernor(Packet packet) {
        if (packet.getType() != PacketTypeEnum.RUN_COMPLETE) {
            resourceManager.addPacket(ProcessEnum.GET_PUT_DATA, new Packet(PacketTypeEnum.RUNNING));
        }
    }

    private void handleMainProc(Packet packet) {
        switch (packet.getData()) {
            case "off" -> resourceManager.addPacket(ProcessEnum.START_STOP, new Packet(PacketTypeEnum.WORK_END));
            case "add" -> resourceManager.addPacket(ProcessEnum.JOB_GOVERNOR, new Packet(PacketTypeEnum.RUNNING));
            case "start" -> resourceManager.addPacket(ProcessEnum.JCL, new Packet(PacketTypeEnum.RUNNING));
        }
    }
}
