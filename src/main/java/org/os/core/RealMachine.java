package org.os.core;

import com.sun.jdi.VMOutOfMemoryException;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.System.out;
import static org.os.userland.InteractiveInterface.VM_ADDRESS;

public record RealMachine(RealMemory realMemory, Cpu cpu, MemoryManager memoryManager, PaginationTable paginationTable,
                          SupervisorMemory supervisorMemory) {

    public int load(String programName) {
        try {
            out.println("Loading the program " + programName);
            CodeInterpreter codeInterpreter = new CodeInterpreter();
            File file = new File(programName);
            if (!file.exists()) {
                out.println("File not found");
                return -1;
            }

            cpu.setModeEnum(ModeEnum.SUPERVISOR);
            int vmId = createVM();
            cpu.setModeEnum(ModeEnum.USER);

            cpu.setAtm(0);
            cpu.setCs(0);
            codeInterpreter.load(memoryManager, file, cpu);
            cpu.setAtm(0);
            cpu.setCs(0);
            cpu.setExc(0);

            cpu.setModeEnum(ModeEnum.SUPERVISOR);
            return vmId;
        } catch (ArrayIndexOutOfBoundsException e) {
            cpu.setExc(ExceptionEnum.OUT_OF_BOUNDS.getValue());
            handleException();
        } catch (VMOutOfMemoryException | OutOfMemoryError e) {
            cpu.setExc(ExceptionEnum.MEMORY_ERROR.getValue());
            handleException();
        } catch (RuntimeException e) {
            cpu.setExc(ExceptionEnum.RUNTIME_EXCEPTION.getValue());
            handleException();
        }
        return -1;
    }


    public void clear(int ptr) throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
        memoryManager.free(ptr);
        out.println("Program at index " + ptr + " cleared");
    }

    public void preRun(int ptr) throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
        cpu.setModeEnum(ModeEnum.USER);
        cpu.setAr((int) memoryManager.memory().readLower(ptr * 16));
        cpu.setBr((int) memoryManager.memory().readLower(ptr * 16 + 1));
        cpu.setAtm((int) memoryManager.memory().readLower(ptr * 16 + 2));
        cpu.setTf((int) memoryManager.memory().readLower(ptr * 16 + 3));
        cpu.setPtr((int) memoryManager.memory().readLower(ptr * 16 + 4));

        cpu.setPtr(ptr);
    }

    public void virtualMachineInterrupt() throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
        int address = cpu.getPtr() * 16;

        memoryManager.memory().writeLower(address, cpu.getAr());
        memoryManager.memory().writeLower(address + 1, cpu.getBr());
        memoryManager.memory().writeLower(address + 2, cpu.getAtm());
        memoryManager.memory().writeLower(address + 3, cpu.getTf());
        memoryManager.memory().writeLower(address + 4, cpu.getPtr());

        handleException();
        cpu.setModeEnum(ModeEnum.SUPERVISOR);
        cpu.setTi(0);
    }

    public String continueRun(int ptr) {
        try {
            long command = memoryManager.read(cpu.getAtm(), ptr);
            if (CodeEnum.byCode(command) == CodeEnum.EMPTY) {
                cpu.setExc(ExceptionEnum.HALT.getValue());
                return "[EMPTY]";
            } else {
                handleCommand(command);
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                return "Command: " + CodeEnum.byCode(command) + "\n" + cpu + "\nTime: " + currentTime;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            cpu.setExc(ExceptionEnum.OUT_OF_BOUNDS.getValue());
            handleException();
        } catch (VMOutOfMemoryException | OutOfMemoryError e) {
            cpu.setExc(ExceptionEnum.MEMORY_ERROR.getValue());
            handleException();
        } catch (RuntimeException e) {
            cpu.setExc(ExceptionEnum.RUNTIME_EXCEPTION.getValue());
            handleException();
        }
        return "";
    }

    public void run(int ptr, int cycleTimes) throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
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
    public void runSuper(int cycles) throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
        for (int i = 0; i < cycles; i++) {
            continueRun(-1);
        }
    }

    public void handleCommand(long val) throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException, ArithmeticException {
        CodeEnum command = CodeEnum.byCode(val);

        int atm = switch (command) {
            case ADD:
                cpu.setAr(Math.addExact(cpu.getAr(), cpu.getBr()));
                yield 1;
            case SUB:
                cpu.setAr(cpu.getAr() - cpu.getBr());
                yield 1;
            case DIV:
                cpu.setAr(cpu.getAr() / cpu.getBr());
                cpu.setBr(cpu.getAr() % cpu.getBr());
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
            case MOVE:
                yield handleMoveCommand();
            case HALT:
                cpu.setExc(ExceptionEnum.HALT.getValue());
                yield 0;
            case DEL:
                cpu.setExc(ExceptionEnum.DEL.getValue());
                yield 0;
            case DIV_ZERO:
                cpu.setExc(ExceptionEnum.DIVISION_BY_ZERO.getValue());
                yield 0;
            case OVERFLOW:
                cpu.setExc(ExceptionEnum.OVERFLOW.getValue());
                yield 0;
            case OUT_OF_MEMORY:
                cpu.setExc(ExceptionEnum.OUT_OF_MEMORY.getValue());
                yield 0;
            case PRINT:
                cpu.setExc(ExceptionEnum.OUTPUT.getValue());
                yield handlePrintCommand();
            case LD:
                cpu.setAr((int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr()));
                yield 2;
            case ST:
                if (cpu.getMode() == 1) {
                    memoryManager.write(cpu.getAtm() + 1, cpu.getAr(), cpu.getPtr());
                } else if (cpu.getMode() == 0) {
                    memoryManager.memory().write(cpu.getAtm() + 1, cpu.getAr());
                }
                yield 2;
            default:
                yield 1;
        };
        cpu.setAtm(cpu.getAtm() + atm);
    }

    private int handlePrintCommand() {
        int atm = 1;
        int index = 0;
        while (memoryManager.read(cpu.getAtm() + atm, cpu.getPtr()) != '$') {
            supervisorMemory.write(index, (int) memoryManager.read(cpu.getAtm() + atm, cpu.getPtr()));
            atm++;
            index++;
        }
        return atm + 1;
    }

    private int handleJmpCommand(int flag) {
        int atm = (int) memoryManager.read(cpu.getAtm() + 1, cpu.getPtr());
        return flag == 1 ? cpu.getAtm() + atm : atm;
    }

    private int handleMoveCommand() throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
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
        }
        return handleRegisterToRegisterMove(reg1, reg2);
    }

    private int handleRegisterToValueMove(long arg1, CodeEnum reg2) throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
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
            memoryManager.memory().write((int) address, (int) registerValue);
        }
        return 3;
    }

    private int handleValueToRegisterMove(CodeEnum reg2, long arg1) throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
        long value = memoryManager.read((int) arg1, cpu.getPtr());

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

    public void handleException() {
        int exception = cpu.getExc();
        ExceptionEnum exceptionEnum = ExceptionEnum.byValue(exception);
        if (exceptionEnum == ExceptionEnum.NO_EXCEPTION) {
            return;
        }
        switch (exceptionEnum) {
            case ExceptionEnum.RUNTIME_EXCEPTION -> {
                out.println("Exception detected: program deleted at index " + cpu.getPtr());
                clear(cpu.getPtr());
            }
            case ExceptionEnum.HALT -> {
                out.println("Program halted at index " + cpu.getPtr());
                clear(cpu.getPtr());
            }
            case ExceptionEnum.DEL -> {
                out.println("Program deleted at index " + cpu.getPtr());
                clear(cpu.getPtr());
            }
            case ExceptionEnum.OUT_OF_MEMORY -> {
                out.println("Out of memory exception detected at index " + cpu.getPtr());
                clear(cpu.getPtr());
            }
            case ExceptionEnum.ARRAY_INDEX_OUT_OF_BOUNDS -> {
                out.println("Array index out of bounds exception detected at index " + cpu.getPtr());
                clear(cpu.getPtr());
            }
        }
        // exception();
    }

    private void exception() throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
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

    private int createVM() throws ArrayIndexOutOfBoundsException, VMOutOfMemoryException {
        int ptr = 0;

        while (memoryManager.read(VM_ADDRESS + ptr, ptr) == 1) {
            ptr++;
        }
        cpu.setPtr(ptr);
        paginationTable.allocate(ptr);
        memoryManager.write(VM_ADDRESS + ptr, 1, ptr);
        return ptr;
    }

    public boolean vmExists(int index) {
        return memoryManager.read(VM_ADDRESS + index, index) == 1;
    }
}
