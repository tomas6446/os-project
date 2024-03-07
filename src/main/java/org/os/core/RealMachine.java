package org.os.core;

public class RealMachine {
    private Cpu cpu;

    public RealMachine() {
        cpu = new Cpu();
    }

    public boolean load(String programName) {
        System.out.println("Loading the program " + programName);
        cpu.setModeEnum(ModeEnum.USER);

        return true;
    }

    public boolean unload(int index) {
        System.out.println("Stopping the program at index " + index);
        return true;
    }

    public boolean run(int index) {
        System.out.println("Running the program at index " + index);
        return true;
    }

    public boolean next() {
        System.out.println("Running the next program");
        return true;
    }

    public boolean interrupt() {
        System.out.println("Interrupting the current program");
        return true;
    }
}
