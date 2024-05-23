package org.os.processes;

import lombok.Data;
import org.os.core.RealMachine;

import java.util.EnumMap;
import java.util.Map;

@Data
public class ResourceManager {
    private final Map<ProcessEnum, ProcessNode> processes;
    private final RealMachine realMachine;

    public ResourceManager(RealMachine realMachine) {
        this.realMachine = realMachine;
        this.processes = new EnumMap<>(ProcessEnum.class);
    }

    public void addProcess(ProcessEnum processName) {
        addPacket(PacketTypeEnum.START_NEW_PROCESS, processName, "-");
        processes.computeIfAbsent(processName, ProcessNode::new);
    }

    public void addPacket(PacketTypeEnum packetType, ProcessEnum to, String data) {
        Packet packet = new Packet(packetType, to, data);
        processes.computeIfAbsent(to, ProcessNode::new).addPacket(packet);
    }

    public ProcessNode getProcessNode(ProcessEnum process) {
        return processes.get(process);
    }
}
