package org.os.processes;

public class EnvironmentInteraction {
    public Packet interact(Packet packet) {
        return switch (packet) {
            case INPUT -> handleInputPacket(packet);
            case OUTPUT -> handleOutputPacket(packet);
            default -> packet;
        };
    }

    private Packet handleOutputPacket(Packet packet) {
        System.out.println(packet.getData());
        // getPutData.interact(packet);
        return packet;
    }

    private Packet handleInputPacket(Packet packet) {
        return switch (packet.getData()) {
            case "off" -> Packet.WORK_END_U_OFF;
            case "add" -> Packet.WORK_END_U_ADD;
            case "start" -> Packet.WORK_END_U_START;
            default -> Packet.INPUT;
        };
    }
}
