package org.os.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionEnum {
    NO_EXCEPTION(0, "No exception"),
    RUNTIME_EXCEPTION(1, "Runtime exception"),
    DIVISION_BY_ZERO(2, "Division by zero"),
    ARITHMETIC_EXCEPTION(3, "Arithmetic exception"),
    MEMORY_ERROR(4, "Memory error"),
    ARRAY_INDEX_OUT_OF_BOUNDS(5, "Array index out of bounds"),
    NULL_POINTER_EXCEPTION(6, "Null pointer exception"),
    ILLEGAL_ARGUMENT_EXCEPTION(7, "Illegal argument exception"),
    ILLEGAL_STATE_EXCEPTION(8, "Illegal state exception"),
    OUT_OF_BOUNDS(9, "Out of bounds"),
    OUT_OF_MEMORY(10, "Out of memory"),
    OVERFLOW(11, "Overflow"),
    HALT(12, "Halt"),
    DEL(13, "Del"),
    INPUT(14, "Input"),
    OUTPUT(15, "Output");

    private final int value;
    private final String name;

    public static ExceptionEnum byValue(int exception) {
        for (ExceptionEnum exceptionEnum : values())
            if (exceptionEnum.value == exception) {
                return exceptionEnum;
            }
        throw new IllegalArgumentException("Unknown exception: " + exception);
    }
}
