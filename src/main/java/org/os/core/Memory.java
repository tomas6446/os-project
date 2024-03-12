package org.os.core;

public interface Memory {
    Word read(int address);
    void write(int address, int value);
}
