package org.os.processes;

import org.os.core.RealMachine;

import java.util.stream.IntStream;

public class Jcl {
    private final Vm vm;
    private final RealMachine realMachine;

    public Jcl(RealMachine realMachine, Vm vm) {
        this.realMachine = realMachine;
        this.vm = vm;
    }

    public Packet interact(Packet packet) {
        IntStream.range(0, 16)
                .filter(realMachine::vmExists)
                .forEach(i -> vm.interact(packet, i));
        return packet;
    }
}
