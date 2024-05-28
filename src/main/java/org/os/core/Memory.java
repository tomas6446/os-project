package org.os.core;

import lombok.Getter;

import java.util.stream.IntStream;

@Getter
public abstract class Memory implements IMemory {
    protected final int size;
    protected final Word[] words;

    protected Memory(int size) {
        this.size = size;
        this.words = new Word[size];
        IntStream.range(0, size).forEachOrdered(i -> words[i] = new Word());
    }

    @Override
    public Word read(int address) {
        return words[address].getWord();
    }

    @Override
    public void write(int address, long value) {
        words[address].fromInt(value);
    }

    @Override
    public void writeLower(int address, int value) {
        words[address].setLeft(value);
    }

    @Override
    public long readLower(int address) {
        return words[address].getLeft();
    }
}
