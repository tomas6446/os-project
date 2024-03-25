package org.os.core;

import lombok.Getter;

import java.io.File;

import static java.lang.System.out;
import static org.os.userland.ComputerInterface.VM_ADDRESS;

@Getter
public class RealMachine {
    private final MemoryManager memoryManager;
    private final Cpu cpu;
    private final RealMemory realMemory;
    private final PaginationTable paginationTable;

    public RealMachine(RealMemory realMemory, Cpu cpu, MemoryManager memoryManager, PaginationTable paginationTable) {
        this.realMemory = realMemory;
        this.cpu = cpu;
        this.memoryManager = memoryManager;
        this.paginationTable = paginationTable;
    }

    public boolean load(String programName) {
        out.println("Loading the program " + programName);

        CodeInterpreter codeInterpreter = new CodeInterpreter();
        cpu.setModeEnum(ModeEnum.USER);
        createVM();

        File file = new File(programName);
        codeInterpreter.load(memoryManager, file, cpu);

        out.println("Run the program with the command run " + cpu.getPtr());
        cpu.setAtm(0);
        cpu.setModeEnum(ModeEnum.SUPERVISOR);

        return true;
    }


    public boolean clear(int ptr) {
        paginationTable.free(ptr);
        return true;
    }

    public boolean run(int ptr) {
        cpu.setModeEnum(ModeEnum.USER);

        out.println("Running the program at index " + ptr);
        int cycleTimes = 6;

        cpu.setAr((int) memoryManager.getMemory().readLower(ptr));
        cpu.setBr((int) memoryManager.getMemory().readLower(ptr + 1));
        cpu.setAtm((int) memoryManager.getMemory().readLower(ptr + 2));
        cpu.setIc((int) memoryManager.getMemory().readLower(ptr + 3));
        cpu.setTf((int) memoryManager.getMemory().readLower(ptr + 4));
        cpu.setPtr((int) memoryManager.getMemory().readLower(ptr + 5));

        while (cycleTimes > 0) {
            cycleTimes--;
            long command = memoryManager.read(cpu.getAtm(), ptr);
            int atm = handleCommand(command);
            cpu.setAtm(cpu.getAtm() + atm);
        }
        virtualMachineInterrupt();

        return true;
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
                out.println(cpu.getAr());
                return 1;
            default:
                return 1;
        }
    }

    private int handleJmpCommand(int flag) {
        return flag == 1 ? cpu.getAtm() + (int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr()) :
                (int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr());
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
        }
    }

    public void virtualMachineInterrupt() {
        out.println("Interrupting the current program");

        int address = cpu.getPtr();
        memoryManager.getMemory().writeLower(address, cpu.getAr());
        memoryManager.getMemory().writeLower(address + 1, cpu.getBr());
        memoryManager.getMemory().writeLower(address + 2, cpu.getAtm());
        memoryManager.getMemory().writeLower(address + 3, cpu.getIc());
        memoryManager.getMemory().writeLower(address + 4, cpu.getTf());
        memoryManager.getMemory().writeLower(address + 5, cpu.getPtr());

        handleException();
        cpu.setModeEnum(ModeEnum.SUPERVISOR);
    }

    private void handleException() {
        if (cpu.getExc() == 0) {
            out.println("Stopping the program");
        } else if (cpu.getExc() == 1) {
            out.println("Deleting the program");
            clear(cpu.getPtr());
        }
    }

    private void createVM() {
        ModeEnum lastMode = cpu.getModeEnum();
        cpu.setModeEnum(ModeEnum.SUPERVISOR);

        int ptr = 0;
        while (memoryManager.read(VM_ADDRESS + ptr, ptr) == 1) {
            ptr++;
        }

        cpu.setPtr(ptr);
        paginationTable.allocate(ptr);
        memoryManager.write(VM_ADDRESS + ptr, 1, ptr);

        cpu.setModeEnum(lastMode);
    }
}
