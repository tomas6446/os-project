package org.os.processes;

public class EnvironmentInteraction {
    private final GetPutData getPutData;

    public EnvironmentInteraction(GetPutData getPutData) {
        this.getPutData = getPutData;
    }

    public Packet interact(Packet packet) {
        return switch (packet) {
            case INPUT_I, INPUT -> handleInputPacket(packet);
            case OUTPUT -> handleOutputPacket(packet);
            default -> packet;
        };
    }

    private Packet handleOutputPacket(Packet packet) {
        /* Ikrauti faila? */
        /* Taip -> Isvesti Informacija*/

        getPutData.interact(packet);
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
