package org.os.proc;

import org.os.core.ExceptionEnum;
import org.os.core.ModeEnum;
import org.os.core.RealMachine;
import org.os.userland.ProcessManagerGUI;

import java.io.IOException;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Planner {
    private boolean isWorking = true;
    private final ResourceManager resourceManager;
    private final RealMachine realMachine;
    private final Scanner scanner = new Scanner(System.in);
    private final ProcessManagerGUI processManagerGUI;
    private StringBuilder userInputBuffer = new StringBuilder();

    public Planner(RealMachine realMachine, ResourceManager resourceManager) {
        this.realMachine = realMachine;
        this.resourceManager = resourceManager;
        this.processManagerGUI = new ProcessManagerGUI();

        resourceManager.addPacket(ProcessEnum.START_STOP, new Packet(PacketTypeEnum.RUNNING));
    }

    public void plan() {
        while (isWorking) {
            for (ProcessEnum process : ProcessEnum.values()) {
                var stack = resourceManager.getProcessPackets(process);
                if (!stack.isEmpty()) {
                    Packet packet = process == ProcessEnum.ENVIRONMENT_INTERACTION ||
                            process == ProcessEnum.JCL ||
                            process == ProcessEnum.VM ?
                            stack.peek() :
                            stack.pop();
                    handleProcess(process, packet);
                }
                processManagerGUI.refreshTable(resourceManager.getProcesses());
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
                case JCL -> handleJcl(packet);
                case GET_PUT_DATA -> handleGetPutData(packet);
                case START_STOP -> handleStartStop(packet);
                case VM -> handleVm(packet);
            }
        } catch (IOException e) {
            System.out.println("Error while handling packet: " + packet);
        } finally {
            if (process != ProcessEnum.ENVIRONMENT_INTERACTION) {
                resourceManager.removePacket(process, packet); // Ensure packet is removed after processing
            }
        }
    }

    private void handleStartStop(Packet packet) {
        if (packet.getType() == PacketTypeEnum.WORK_END) {
            isWorking = false;
        }

        resourceManager.addPacket(ProcessEnum.ENVIRONMENT_INTERACTION, new Packet(PacketTypeEnum.RUNNING));
        resourceManager.addPacket(ProcessEnum.INTERRUPT, new Packet(PacketTypeEnum.RUNNING));
        resourceManager.addPacket(ProcessEnum.MAIN_PROC, new Packet(PacketTypeEnum.RUNNING));
        resourceManager.addPacket(ProcessEnum.JCL, new Packet(PacketTypeEnum.RUNNING));
    }

    private void handleVm(Packet packet) {
        if (packet.getType() != PacketTypeEnum.RUN_VM) {
            return;
        }
        int vmId = Integer.parseInt(packet.getData());
        realMachine.getCpu().setModeEnum(ModeEnum.USER);
        realMachine.continueRun(vmId);

        ExceptionEnum exception = ExceptionEnum.byValue(realMachine.getCpu().getExc());
        switch (exception) {
            case OUTPUT -> resourceManager.addPacket(ProcessEnum.ENVIRONMENT_INTERACTION, new Packet(PacketTypeEnum.OUTPUT));
            case INPUT -> resourceManager.addPacket(ProcessEnum.ENVIRONMENT_INTERACTION, new Packet(PacketTypeEnum.INPUT_I));
            case HALT -> resourceManager.removePacket(ProcessEnum.VM, packet);
        }
        realMachine.getCpu().setModeEnum(ModeEnum.SUPERVISOR);
    }

    private void handleGetPutData(Packet packet) {
        if (packet.getType() == PacketTypeEnum.OUTPUT) {
            System.out.println(packet.getData());
            return;
        }
        System.out.println("Program path:");

        String input = scanner.nextLine();
        userInputBuffer.append(input);
        processManagerGUI.updateUserInputBuffer(userInputBuffer.toString());
        String data = userInputBuffer.toString();
        processManagerGUI.updateUserInputBuffer(userInputBuffer.toString());
        realMachine.load(data);

        userInputBuffer = new StringBuilder();
        resourceManager.addPacket(ProcessEnum.JOB_GOVERNOR, new Packet(PacketTypeEnum.RUN_COMPLETE));
    }

    private void handleJcl(Packet packet) {
        if (packet.getType() != PacketTypeEnum.NEW_VM_TO_LIST) {
            return;
        }
        boolean vmExists = IntStream.range(0, 15).anyMatch(realMachine::vmExists);

        if (vmExists) {
            IntStream.range(0, 15).filter(realMachine::vmExists).forEachOrdered(i ->
                    resourceManager.addPacket(ProcessEnum.VM, new Packet(PacketTypeEnum.RUN_VM, String.valueOf(i))));
        } else {
            resourceManager.removePacket(ProcessEnum.JCL, packet);
        }
    }

    private void handleInterrupt() {
        ExceptionEnum exception = ExceptionEnum.byValue(realMachine.getCpu().getExc());
        realMachine.handleException();
        if (exception == ExceptionEnum.NO_EXCEPTION) {
            return;
        }
        resourceManager.addPacket(ProcessEnum.ENVIRONMENT_INTERACTION, new Packet(PacketTypeEnum.OUTPUT, "Exception thrown: " + exception));
    }

    private void handleEnvironmentInteraction(Packet packet) throws IOException {
        switch (packet.getType()) {
            case INPUT_I, RUNNING -> {
                // Check for user input and process it
                if (System.in.available() > 0) {
                    String input = scanner.nextLine();
                    userInputBuffer.append(input);
                    processManagerGUI.updateUserInputBuffer(userInputBuffer.toString());
                }
                String data = userInputBuffer.toString();
                if (data.contains("clearbuff")) {
                    userInputBuffer = new StringBuilder();
                    processManagerGUI.updateUserInputBuffer(userInputBuffer.toString());
                }
                switch (data) {
                    case "off" -> {
                        resourceManager.addPacket(ProcessEnum.MAIN_PROC, new Packet(PacketTypeEnum.WORK_END_U, "off"));
                        userInputBuffer = new StringBuilder();
                    }
                    case "add" -> {
                        resourceManager.addPacket(ProcessEnum.MAIN_PROC, new Packet(PacketTypeEnum.WORK_END_U, "add"));
                        userInputBuffer = new StringBuilder();
                    }
                }
            }
            case OUTPUT -> resourceManager.addPacket(ProcessEnum.GET_PUT_DATA, new Packet(PacketTypeEnum.OUTPUT, packet.getData()));
        }
    }

    private void handleJobGovernor(Packet packet) {
        if (packet.getType() != PacketTypeEnum.RUN_COMPLETE) {
            resourceManager.addPacket(ProcessEnum.GET_PUT_DATA, new Packet(PacketTypeEnum.RUNNING));
        }
        resourceManager.addPacket(ProcessEnum.JCL, new Packet(PacketTypeEnum.NEW_VM_TO_LIST));
    }

    private void handleMainProc(Packet packet) {
        switch (packet.getData()) {
            case "off" -> resourceManager.addPacket(ProcessEnum.START_STOP, new Packet(PacketTypeEnum.WORK_END));
            case "add" -> resourceManager.addPacket(ProcessEnum.JOB_GOVERNOR, new Packet(PacketTypeEnum.RUNNING));
        }
    }
}
