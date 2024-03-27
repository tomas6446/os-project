package org.os.core;

import com.sun.jdi.VMOutOfMemoryException;
import lombok.Getter;

import java.io.File;

import static java.lang.System.out;
import static org.os.userland.ComputerInterface.VM_ADDRESS;

@Getter
public class RealMachine {
    private final MemoryManager memoryManager;
    private final RealMemory realMemory;
    private final PaginationTable paginationTable;
    private final Cpu cpu;

    public RealMachine(RealMemory realMemory, Cpu cpu, MemoryManager memoryManager, PaginationTable paginationTable) {
        this.realMemory = realMemory;
        this.cpu = cpu;
        this.memoryManager = memoryManager;
        this.paginationTable = paginationTable;
    }

    public void load(String programName) {
        out.println("Loading the program " + programName);

        cpu.setModeEnum(ModeEnum.SUPERVISOR);
        createVM();
        cpu.setModeEnum(ModeEnum.USER);

        CodeInterpreter codeInterpreter = new CodeInterpreter();
        File file = new File(programName);
        codeInterpreter.load(memoryManager, file, cpu);
        cpu.setAtm(0);

        cpu.setModeEnum(ModeEnum.SUPERVISOR);
    }


    public void clear(int ptr) {
        memoryManager.free(ptr);
        out.println("Program at index " + ptr + " cleared");
    }

    public void preRun(int ptr) {
        cpu.setModeEnum(ModeEnum.USER);
        out.println("Prerunning the program at index " + ptr);
        try {
            cpu.setAr((int) memoryManager.getMemory().readLower(ptr * 16));
            cpu.setBr((int) memoryManager.getMemory().readLower(ptr * 16 + 1));
            cpu.setAtm((int) memoryManager.getMemory().readLower(ptr * 16 + 2));
            cpu.setTf((int) memoryManager.getMemory().readLower(ptr * 16 + 3));
            cpu.setPtr((int) memoryManager.getMemory().readLower(ptr * 16 + 4));
        } catch (ArrayIndexOutOfBoundsException | VMOutOfMemoryException e) {
            cpu.setExc(4);
            virtualMachineInterrupt();
            return;
        }
        cpu.setPtr(ptr);
    }

    public void virtualMachineInterrupt() {
        out.println("Interrupting the current program");

        int address = cpu.getPtr() * 16;
        try {
            memoryManager.getMemory().writeLower(address, cpu.getAr());
            memoryManager.getMemory().writeLower(address + 1, cpu.getBr());
            memoryManager.getMemory().writeLower(address + 2, cpu.getAtm());
            memoryManager.getMemory().writeLower(address + 3, cpu.getTf());
            memoryManager.getMemory().writeLower(address + 4, cpu.getPtr());
        } catch (ArrayIndexOutOfBoundsException e) {
            cpu.setExc(4);
            virtualMachineInterrupt();
            return;
        }

        handleException();
        cpu.setModeEnum(ModeEnum.SUPERVISOR);
        cpu.setTi(0);
        out.println("Program interrupted");
    }

    public void continueRun(int ptr) {
        long command = 0;
        try {
            command = memoryManager.read(cpu.getAtm(), ptr);
        } catch (VMOutOfMemoryException | ArrayIndexOutOfBoundsException e) {
            cpu.setExc(4);
            virtualMachineInterrupt();
            return;
        } catch (Exception e) {
            out.println("Error while reading memory: " + e.getMessage());
        }
        out.println("Command: " + CodeEnum.byCode(command) + "\n" + cpu);
        handleCommand(command);
    }

    public void run(int ptr, int cycleTimes) {
        preRun(ptr);
        for (; cpu.getTi() < cycleTimes; cpu.setTi(cpu.getTi() + 1)) {
            continueRun(ptr);
        }
        virtualMachineInterrupt();
    }

    /*
     * This method is used to run the program in super mode
     * -1 denotes that the program is running in super mode (RAND INT)
     */
    public void runSuper(int cycles) {
        for (int i = 0; i < cycles; i++) {
            continueRun(-1);
        }
    }

    public void handleCommand(long val) {
        CodeEnum command = CodeEnum.byCode(val);
        try {
            int atm = switch (command) {
                case ADD:
                    cpu.setAr(Math.addExact(cpu.getAr(), cpu.getBr()));
                    yield 1;
                case SUB:
                    cpu.setAr(cpu.getAr() - cpu.getBr());
                    yield 1;
                case DIV:
                    try {
                        cpu.setAr(cpu.getAr() / cpu.getBr());
                        cpu.setBr(cpu.getAr() % cpu.getBr());
                    } catch (ArithmeticException e) {
                        cpu.setExc(2);
                        virtualMachineInterrupt();
                    }
                    yield 1;
                case MUL:
                    cpu.setAr(cpu.getAr() * cpu.getBr());
                    yield 1;
                case NEG:
                    cpu.setAr(-cpu.getAr());
                    yield 1;
                case AND:
                    cpu.setAr(cpu.getAr() & cpu.getBr());
                    yield 1;
                case OR:
                    cpu.setAr(cpu.getAr() | cpu.getBr());
                    yield 1;
                case NOT:
                    cpu.setAr(~cpu.getAr());
                    yield 1;
                case CMP:
                    cpu.setTf(cpu.getAr() == cpu.getBr() ? 1 : 0);
                    yield 1;
                case JL:
                    cpu.setAtm(cpu.getAr() > cpu.getBr() ? handleJmpCommand(0) : 2);
                    yield 0;
                case JG:
                    cpu.setAtm(cpu.getAr() < cpu.getBr() ? handleJmpCommand(0) : 2);
                    yield 0;
                case JM:
                    cpu.setAtm(handleJmpCommand(0));
                    yield 0;
                case JMR:
                    yield handleJmpCommand(1);
                case JLR:
                    yield cpu.getAr() > cpu.getBr() ? handleJmpCommand(1) : 2;
                case JGR:
                    yield cpu.getAr() < cpu.getBr() ? handleJmpCommand(1) : 2;
                case LD:
                    cpu.setAr((int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr()));
                    yield 2;
                case ST:
                    switch (cpu.getMode()) {
                        case 1 -> memoryManager.write(cpu.getAtm() + 1, cpu.getAr(), cpu.getPtr());
                        case 0 -> memoryManager.getMemory().write(cpu.getAtm() + 1, cpu.getAr());
                    }
                    yield 2;
                case MOVE:
                    yield handleMoveCommand();
                case HALT:
                    cpu.setExc(0);
                    yield 0;
                case DEL:
                    cpu.setExc(1);
                    yield 0;
                case DIV_ZERO:
                    cpu.setExc(2);
                    yield 0;
                case OVERFLOW:
                    cpu.setExc(3);
                    yield 0;
                case OUT_OF_MEMORY:
                    cpu.setExc(4);
                    yield 0;
                case PRINT:
                    out.println(cpu.getAr());
                    yield 1;
                default:
                    yield 1;
            };
            cpu.setAtm(cpu.getAtm() + atm);
        } catch (VMOutOfMemoryException e) {
            cpu.setExc(4);
            virtualMachineInterrupt();
        } catch (ArithmeticException e) {
            cpu.setExc(3);
            virtualMachineInterrupt();
        }
    }

    private int handleJmpCommand(int flag) {
        int atm;
        try {
            atm = (int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr());
        } catch (VMOutOfMemoryException | ArrayIndexOutOfBoundsException e) {
            cpu.setExc(4);
            virtualMachineInterrupt();
            return 0;
        }
        return flag == 1 ? cpu.getAtm() + atm : atm;
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
        long value;
        try {
            value = memoryManager.read((int) arg1, cpu.getPtr());
        } catch (VMOutOfMemoryException | ArrayIndexOutOfBoundsException e) {
            cpu.setExc(4);
            virtualMachineInterrupt();
            return 0;
        }
        switch (reg2) {
            case AR -> cpu.setAr((int) value);
            case BR -> cpu.setBr((int) value);
            case ATM -> cpu.setAtm((int) value);
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
            case PTR -> cpu.setPtr(value);
            case TF -> cpu.setTf(value);
            default -> throw new IllegalStateException("Unexpected value: " + reg2);
        }
    }

    private void handleException() {
        if (cpu.getExc() == 0) {
            out.println("Exception detected: program halted");
        } else if (cpu.getExc() == 1) {
            out.println("Exception detected: program deleted");
            clear(cpu.getPtr());
        } else if (cpu.getExc() == 2) {
            out.println("Exception detected: division by zero");
            exception();
        } else if (cpu.getExc() == 3) {
            out.println("Exception detected: overflow");
            exception();
        } else if (cpu.getExc() == 4) {
            out.println("Exception detected: memory error");
            exception();
        }
    }

    private void exception() {
        cpu.setModeEnum(ModeEnum.SUPERVISOR);
        int atm = cpu.getAtm();
        int cs = cpu.getCs();
        cpu.setAtm(4320);
        cpu.setCs(4320);
        for (int i = 0; i < 5; i++) {
            continueRun(-1);
        }
        cpu.setAtm(atm);
        cpu.setCs(cs);
    }

    private void createVM() {
        int ptr = 0;
        try {
            while (memoryManager.read(VM_ADDRESS + ptr, ptr) == 1) {
                ptr++;
            }
            cpu.setPtr(ptr);
            paginationTable.allocate(ptr);
            memoryManager.write(VM_ADDRESS + ptr, 1, ptr);
        } catch (ArrayIndexOutOfBoundsException | VMOutOfMemoryException e) {
            cpu.setExc(4);
            virtualMachineInterrupt();
        }
    }

    public boolean vmExists(int index) {
        return memoryManager.read(VM_ADDRESS + index, index) == 1;
    }
}
