package org.os.core;

import lombok.Getter;

import java.util.stream.IntStream;

@Getter
public class SupervisorMemory implements Memory {
    private final int size;
    private final Word[] memory;

    public SupervisorMemory(int size) {
        this.size = size;
        this.memory = new Word[size];
        IntStream.range(0, size).forEachOrdered(i -> memory[i] = new Word());
    }

    @Override
    public Word read(int address) {
        return memory[address].getWord();
    }

    @Override
    public void write(int address, long value) {
        memory[address].fromInt(value);
    }

    @Override
    public void writeLower(int address, int value) {
        memory[address].setLeft(value);
    }

    @Override
    public long readLower(int address) {
        return memory[address].getLeft();
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (memory[i].isFree()) {
                break;
            }
            sb.append(memory[i].toString());
        }
        clearBuff();
        return sb.toString();
    }

    private void clearBuff() {
        for (int i = 0; i < size; i++) {
            memory[i] = new Word();
        }
    }
}
