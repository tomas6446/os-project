package org.os.vm;

public record VirtualCpuRegisters(
        int ar,
        int br,
        int atm,
        int ic,
        int tf,
        int ptr) {
}
