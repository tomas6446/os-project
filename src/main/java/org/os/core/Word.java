package org.os.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Word {
    public static final int SIZE = 4;
    private byte[] upper; // 2 bytes, always stores data
    private byte[] lower; // 2 bytes, always stores registers

    public Word() {
        upper = new byte[SIZE / 2];
        lower = new byte[SIZE / 2];
    }

    public int toInt() {
        return (lower[0] & 0xFF) << 24 | (lower[1] & 0xFF) << 16 | (upper[0] & 0xFF) << 8 | (upper[1] & 0xFF);
    }

    public void fromInt(int value) {
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
        return String.format("%s %s\n%s %s",
                String.format("%8s", Integer.toBinaryString(upper[0] & 0xFF)).replace(' ', '0'),
                String.format("%8s", Integer.toBinaryString(upper[1] & 0xFF)).replace(' ', '0'),
                String.format("%8s", Integer.toBinaryString(lower[0] & 0xFF)).replace(' ', '0'),
                String.format("%8s", Integer.toBinaryString(lower[1] & 0xFF)).replace(' ', '0')
        );
    }
}
