package org.os.core;

public record CpuRegisters(
        int ar,
        int br,
        int ic,
        int tf,
        int ptr,
        int cs,
        int arm,
        int flag,
        int exc,
        int ti,
        int pi,
        int mode
) {
}
