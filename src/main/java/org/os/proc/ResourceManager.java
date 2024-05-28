package org.os.proc;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ResourceManager {
    private final Map<ProcessEnum, Deque<Packet>> processPackets = new HashMap<>();

    public ResourceManager() {
        for (ProcessEnum process : ProcessEnum.values()) {
            processPackets.put(process, new LinkedList<>());
        }
    }

    public void addPacket(ProcessEnum process, Packet packet) {
        Deque<Packet> queue = processPackets.get(process);
        if (queue != null) {
            queue.addLast(packet); // Add to the end of the queue
        }
    }

    public void addPacketToFront(ProcessEnum process, Packet packet) {
        Deque<Packet> queue = processPackets.get(process);
        if (queue != null) {
            queue.addFirst(packet); // Add to the front of the queue
        }
    }

    public Deque<Packet> getProcessPackets(ProcessEnum process) {
        return processPackets.get(process);
    }

    public void removePacket(ProcessEnum process, Packet packet) {
        Deque<Packet> queue = processPackets.get(process);
        if (queue != null) {
            queue.remove(packet);
        }
    }
}
