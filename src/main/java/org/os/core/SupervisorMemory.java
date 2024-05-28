package org.os.core;

public class SupervisorMemory extends Memory {
    public SupervisorMemory(int size) {
        super(size);
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (words[i].isFree()) {
                break;
            }
            sb.append(words[i].toString());
        }
        clearBuff();
        return sb.toString();
    }

    private void clearBuff() {
        for (int i = 0; i < size; i++) {
            words[i] = new Word();
        }
    }
}
