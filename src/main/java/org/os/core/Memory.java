package org.os.core;

public interface Memory {
    Word read(int address);

    void write(int address, long value);

    void writeLower(int address, int value);

    long readLower(int address);
}
