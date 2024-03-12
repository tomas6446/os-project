package org.os.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Word {
    private static final int SIZE = 4;
    private byte[] bytes;

    public Word() {
        bytes = new byte[SIZE];
    }

    public Word(int address) {
        bytes = new byte[SIZE];
        fromInt(address);
    }

    public int toInt() {
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public void fromInt(int value) {
        bytes[0] = (byte) (value >> 24);
        bytes[1] = (byte) (value >> 16);
        bytes[2] = (byte) (value >> 8);
        bytes[3] = (byte) value;
    }

    public Word getWord() {
        return this;
    }

    public boolean isFree() {
        return bytes[0] == 0 && bytes[1] == 0 && bytes[2] == 0 && bytes[3] == 0;
    }
}
