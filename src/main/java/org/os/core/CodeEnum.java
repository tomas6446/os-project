package org.os.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandEnum {
    ADD("ADD", 0x00),
    SUB("SUB", 0x01),
    MUL("MUL", 0x02),
    DIV("DIV", 0x03),
    NEG("NEG", 0x04),
    AND("AND", 0x05),
    OR("OR", 0x06),
    NOT("NOT", 0x07),
    CMP("CMP", 0x08),
    LD("LD", 0x09),
    ST("ST", 0x0A),
    MOVE("MOVE", 0x0B),
    VAL("VAL", 0x0C),
    JM("JM", 0x0D),
    HALT("HALT", 0x0E),
    CHAR("CHAR", 0x0F),
    PRINTC("PRINTC", 0x12),
    AR("AR",  80_000_000),
    BR("BR", 80_000_001);

    private final String name;
    private final int code;
}
