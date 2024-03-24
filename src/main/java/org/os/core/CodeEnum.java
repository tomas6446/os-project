package org.os.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeEnum {
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
    PRINT("PRINT", 0x12),
    AR("AR", 0x80000001L),
    BR("BR", 0x80000002L),
    CR("CR", 0x80000003L),
    TF("TF", 0x80000004L),
    PTR("PTR", 0x80000005L),
    CS("CS", 0x80000006L),
    ATM("ATM", 0x80000007L),
    FLAG("FLAG", 0x80000008L),
    EXE("EXE", 0x80000009L),
    PI("PI", 0x80000010L),
    MODE("MODE", 0x80000011L);

    private final String name;
    private final long code;
}
