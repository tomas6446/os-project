package org.os.core;

public interface IMemory {
    Word read(int address);

    void write(int address, long value);

    void writeLower(int address, int value);

    long readLower(int address);
}
