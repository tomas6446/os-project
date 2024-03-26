package org.os.core;

import lombok.Getter;

import java.io.File;

import static org.os.userland.ComputerInterface.VM_ADDRESS;

@Getter
public class RealMachine {
    private final MemoryManager memoryManager;
    private Cpu cpu;
    private final RealMemory realMemory;
    private final PaginationTable paginationTable;

    public RealMachine(RealMemory realMemory, Cpu cpu, MemoryManager memoryManager, PaginationTable paginationTable) {
        this.realMemory = realMemory;
        this.cpu = cpu;
        this.memoryManager = memoryManager;
        this.paginationTable = paginationTable;
    }

    public void load(String programName) {
        System.out.println("Loading the program " + programName);

        cpu.setModeEnum(ModeEnum.SUPERVISOR);
        createVM();
        cpu.setModeEnum(ModeEnum.USER);

        CodeInterpreter codeInterpreter = new CodeInterpreter();
        File file = new File(programName);
        codeInterpreter.load(memoryManager, file, cpu);
        cpu.setAtm(0);

        cpu.setModeEnum(ModeEnum.SUPERVISOR);

        System.out.println("Program loaded. Use 'run' command to start the program");
    }


    public void clear(int ptr) {
        memoryManager.free(ptr);
        System.out.println("Program at index " + ptr + " cleared");
    }

    public void preRun(int ptr) {
        cpu.setModeEnum(ModeEnum.USER);
        System.out.println("Prerunning the program at index " + ptr);
        cpu.setAr((int) memoryManager.getMemory().readLower(ptr * 16));
        cpu.setBr((int) memoryManager.getMemory().readLower(ptr * 16 + 1));
        cpu.setAtm((int) memoryManager.getMemory().readLower(ptr * 16 + 2));
        cpu.setIc((int) memoryManager.getMemory().readLower(ptr * 16 + 3));
        cpu.setTf((int) memoryManager.getMemory().readLower(ptr * 16 + 4));
        cpu.setPtr((int) memoryManager.getMemory().readLower(ptr * 16 + 5));
        cpu.setPtr(ptr);
    }

    public void virtualMachineInterrupt() {
        System.out.println("Interrupting the current program");

        int address = cpu.getPtr() * 16;
        memoryManager.getMemory().writeLower(address, cpu.getAr());
        memoryManager.getMemory().writeLower(address + 1, cpu.getBr());
        memoryManager.getMemory().writeLower(address + 2, cpu.getAtm());
        memoryManager.getMemory().writeLower(address + 3, cpu.getIc());
        memoryManager.getMemory().writeLower(address + 4, cpu.getTf());
        memoryManager.getMemory().writeLower(address + 5, cpu.getPtr());

        handleException();
        cpu.setModeEnum(ModeEnum.SUPERVISOR);
        System.out.println("Program interrupted");
    }

    public long continueRun(int ptr) {
        long command = memoryManager.read(cpu.getAtm(), ptr);
        int atm = handleCommand(command);
        cpu.setAtm(cpu.getAtm() + atm);
        return command;
    }

    public void run(int ptr, int cycleTimes) {
        preRun(ptr);
        while (cycleTimes > 0) {
            cycleTimes--;
            continueRun(ptr);
        }
        virtualMachineInterrupt();
    }

    private int handleCommand(long val) {
        CodeEnum command = CodeEnum.byCode(val);
        switch (command) {
            case ADD:
                cpu.setAr(cpu.getAr() + cpu.getBr());
                return 1;
            case SUB:
                cpu.setAr(cpu.getAr() - cpu.getBr());
                return 1;
            case DIV:
                cpu.setAr(cpu.getAr() / cpu.getBr());
                cpu.setBr(cpu.getAr() % cpu.getBr());
                return 1;
            case MUL:
                cpu.setAr(cpu.getAr() * cpu.getBr());
                return 1;
            case NEG:
                cpu.setAr(-cpu.getAr());
                return 1;
            case AND:
                cpu.setAr(cpu.getAr() & cpu.getBr());
                return 1;
            case OR:
                cpu.setAr(cpu.getAr() | cpu.getBr());
                return 1;
            case NOT:
                cpu.setAr(~cpu.getAr());
                return 1;
            case CMP:
                cpu.setTf(cpu.getAr() == cpu.getBr() ? 1 : 0);
                return 1;
            case JL:
                return cpu.getAr() > cpu.getBr() ? handleJmpCommand(0) : 2;
            case JG:
                return cpu.getAr() < cpu.getBr() ? handleJmpCommand(0) : 2;
            case JM:
                return handleJmpCommand(0);
            case JMR:
                return handleJmpCommand(1);
            case JLR:
                return cpu.getAr() > cpu.getBr() ? handleJmpCommand(1) : 2;
            case JGR:
                return cpu.getAr() < cpu.getBr() ? handleJmpCommand(1) : 2;
            case LD:
                cpu.setAr((int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr()));
                return 2;
            case ST:
                switch (cpu.getMode()) {
                    case 1 -> memoryManager.write(cpu.getAtm() + 1, cpu.getAr(), cpu.getPtr());
                    case 0 -> memoryManager.getMemory().write(cpu.getAtm() + 1, cpu.getAr());
                }
                return 2;
            case MOVE:
                return handleMoveCommand();
            case HALT:
                cpu.setExc(0);
                return 0;
            case DEL:
                cpu.setExc(1);
                return 0;
            case PRINT:
                System.out.println(cpu.getAr());
                return 1;
            default:
                return 1;
        }
    }

    private int handleJmpCommand(int flag) {
        return flag == 1 ? cpu.getAtm() + (int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr()) : (int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr());
    }

    private int handleMoveCommand() {
        long arg1 = memoryManager.read(cpu.getAtm() + 1, cpu.getPtr());
        long arg2 = memoryManager.read(cpu.getAtm() + 2, cpu.getPtr());

        CodeEnum reg1 = CodeEnum.byCode(arg1);
        CodeEnum reg2 = CodeEnum.byCode(arg2);

        boolean isReg1 = arg1 > 0x80000000L;
        boolean isReg2 = arg2 > 0x80000000L;

        if (!isReg1 && isReg2) {
            return handleRegisterToValueMove(arg1, reg2);
        } else if (isReg1 && !isReg2) {
            return handleValueToRegisterMove(reg1, arg2);
        } else {
            return handleRegisterToRegisterMove(reg1, reg2);
        }
    }

    private int handleRegisterToValueMove(long arg1, CodeEnum reg2) {
        long address = memoryManager.read((int) arg1, cpu.getPtr());
        long registerValue = switch (reg2) {
            case AR -> cpu.getAr();
            case BR -> cpu.getBr();
            case ATM -> cpu.getAtm();
            case IC -> cpu.getIc();
            case PTR -> cpu.getPtr();
            case TF -> cpu.getTf();
            default -> throw new IllegalStateException("Unexpected value: " + reg2);
        };
        if (cpu.getMode() == 1) {
            memoryManager.write((int) address, (int) registerValue, cpu.getPtr());
        } else {
            memoryManager.getMemory().write((int) address, (int) registerValue);
        }
        return 3;
    }

    private int handleValueToRegisterMove(CodeEnum reg2, long arg1) {
        long value = memoryManager.read((int) arg1, cpu.getPtr());
        switch (reg2) {
            case AR -> cpu.setAr((int) value);
            case BR -> cpu.setBr((int) value);
            case ATM -> cpu.setAtm((int) value);
            case IC -> cpu.setIc((int) value);
            case PTR -> cpu.setPtr((int) value);
            case TF -> cpu.setTf((int) value);
            default -> throw new IllegalStateException("Unexpected value: " + reg2);
        }
        return 3;
    }

    private int handleRegisterToRegisterMove(CodeEnum reg1, CodeEnum reg2) {
        switch (reg1) {
            case AR -> setRegisterValue(reg2, cpu.getAr());
            case BR -> setRegisterValue(reg2, cpu.getBr());
            case ATM -> setRegisterValue(reg2, cpu.getAtm());
            case IC -> setRegisterValue(reg2, cpu.getIc());
            case PTR -> setRegisterValue(reg2, cpu.getPtr());
            case TF -> setRegisterValue(reg2, cpu.getTf());
            default -> throw new IllegalStateException("Unexpected value: " + reg2);
        }
        return 3;
    }

    private void setRegisterValue(CodeEnum reg2, int value) {
        switch (reg2) {
            case AR -> cpu.setAr(value);
            case BR -> cpu.setBr(value);
            case ATM -> cpu.setAtm(value);
            case IC -> cpu.setIc(value);
            case PTR -> cpu.setPtr(value);
            case TF -> cpu.setTf(value);
            default -> throw new IllegalStateException("Unexpected value: " + reg2);
        }
    }

    private void handleException() {
        if (cpu.getExc() == 1) {
            System.out.println("Deleting the program");
            clear(cpu.getPtr());
        }
    }

    private void createVM() {
        int ptr = 0;
        while (memoryManager.read(VM_ADDRESS + ptr, ptr) == 1) {
            ptr++;
        }

        cpu.setPtr(ptr);
        paginationTable.allocate(ptr);
        memoryManager.write(VM_ADDRESS + ptr, 1, ptr);
    }
}
