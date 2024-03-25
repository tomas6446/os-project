package org.os.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Word {
    public static final int SIZE = 4;
    private byte[] lower; // 2 bytes, always stores registers
    private byte[] upper; // 2 bytes, always stores data

    public Word() {
        lower = new byte[SIZE / 2];
        upper = new byte[SIZE / 2];
    }

    public long toInt() {
        return ((long) (lower[0] & 0xFF) << 24) | ((lower[1] & 0xFF) << 16) | ((upper[0] & 0xFF) << 8) | (upper[1] & 0xFF);
    }

    public void fromInt(long value) {
        lower[0] = (byte) (value >> 24);
        lower[1] = (byte) (value >> 16);
        upper[0] = (byte) (value >> 8);
        upper[1] = (byte) value;
    }


    public Word getWord() {
        return this;
    }

    public boolean isFree() {
        return lower[0] == 0 && lower[1] == 0 && upper[0] == 0 && upper[1] == 0;
    }

    public int getUpper() {
        return (upper[0] & 0xFF) << 8 | (upper[1] & 0xFF);
    }

    public void setUpper(int value) {
        upper[0] = (byte) (value >> 8);
        upper[1] = (byte) value;
    }

    public int getLower() {
        return (lower[0] & 0xFF) << 8 | (lower[1] & 0xFF);
    }

    public void setLower(int value) {
        lower[0] = (byte) (value >> 8);
        lower[1] = (byte) value;
    }

    @Override
    public String toString() {
        return String.format("%02X%02X %02X%02X",
                lower[0] & 0xFF,
                lower[1] & 0xFF,
                upper[0] & 0xFF,
                upper[1] & 0xFF
        );
    }
}
