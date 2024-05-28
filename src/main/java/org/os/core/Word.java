package org.os.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Word {
    public static final int SIZE = 4;
    private byte[] left; // 2 bytes, always stores registers
    private byte[] right; // 2 bytes, always stores data

    public Word() {
        left = new byte[SIZE / 2];
        right = new byte[SIZE / 2];
    }

    public long toInt() {
        return ((long) (left[0] & 0xFF) << 24) | ((left[1] & 0xFF) << 16) | ((right[0] & 0xFF) << 8) | (right[1] & 0xFF);
    }

    public void fromInt(long value) {
        left[0] = (byte) (value >> 24);
        left[1] = (byte) (value >> 16);
        right[0] = (byte) (value >> 8);
        right[1] = (byte) value;
    }


    public Word getWord() {
        return this;
    }

    public boolean isFree() {
        return left[0] == 0 && left[1] == 0 && right[0] == 0 && right[1] == 0;
    }

    public int getRight() {
        return (right[0] & 0xFF) << 8 | (right[1] & 0xFF);
    }

    public void setRight(int value) {
        right[0] = (byte) (value >> 8);
        right[1] = (byte) value;
    }

    public int getLeft() {
        return (left[0] & 0xFF) << 8 | (left[1] & 0xFF);
    }

    public void setLeft(int value) {
        left[0] = (byte) (value >> 8);
        left[1] = (byte) value;
    }

    public String toBinaryString() {
        return String.format("%02X%02X %02X%02X",
                left[0] & 0xFF,
                left[1] & 0xFF,
                right[0] & 0xFF,
                right[1] & 0xFF
        );
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        byte[] dataBytes = new byte[]{right[0], right[1]};
        for (byte b : dataBytes) {
            if (b >= 32 && b <= 126) {
                result.append((char) b);
            }
        }
        return result.toString();
    }
}
