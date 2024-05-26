package org.os.proc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Packet {
    private final PacketTypeEnum type;
    private final String data;

    public Packet(PacketTypeEnum packetTypeEnum) {
        this.type = packetTypeEnum;
        this.data = "-";
    }
}
