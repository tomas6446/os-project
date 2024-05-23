package org.os.processes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Packet {
    private PacketTypeEnum packetType;
    private ProcessEnum to;
    private String data;
}
