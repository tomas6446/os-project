package org.os.proc;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceManager {
    private final Map<ProcessEnum, Queue<Packet>> processes;

    public ResourceManager() {
        processes = Arrays.stream(ProcessEnum.values())
                .collect(Collectors.toMap(
                        process -> process,
                        process -> new LinkedList<>()
                ));
    }

    public void addPacket(ProcessEnum process, Packet packet) {
        processes.computeIfAbsent(process, k -> new LinkedList<>()).add(packet);
    }

    public Queue<Packet> getProcessPackets(ProcessEnum process) {
        return processes.get(process);
    }

    public void removePacket(ProcessEnum processEnum, Packet packet) {
        processes.get(processEnum).remove(packet);
    }

    public Map<ProcessEnum, Queue<Packet>>  getProcesses() {
        return processes;
    }

    public void clearProcess(ProcessEnum processEnum) {
        processes.get(processEnum).clear();
    }
}
