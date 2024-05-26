package org.os.proc;

import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class ResourceManager {
    private final Map<ProcessEnum, Stack<Packet>> processes;

    public ResourceManager() {
        processes = Arrays.stream(ProcessEnum.values())
                .collect(Collectors.toMap(
                        process -> process,
                        process -> new Stack<>()
                ));
    }

    public void addPacket(ProcessEnum process, Packet packet) {
        processes.computeIfAbsent(process, k -> new Stack<>()).push(packet);
    }

    public Stack<Packet> getProcessPackets(ProcessEnum process) {
        return processes.get(process);
    }

    public void removePacket(ProcessEnum processEnum, Packet packet) {
        processes.get(processEnum).remove(packet);
    }

    public Map<ProcessEnum, Stack<Packet>>  getProcesses() {
        return processes;
    }

    public void clearProcess(ProcessEnum processEnum) {
        processes.get(processEnum).clear();
    }
}
