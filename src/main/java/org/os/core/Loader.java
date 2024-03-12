package org.os.core;

import org.os.vm.VirtualMachine;

import java.io.File;

public class Loader {
    private CommandEnum[] commands = CommandEnum.values();

    public VirtualMachine load(RealMachine realMachine, File file, VirtualMachine virtualMachine) {
        return virtualMachine;
    }
}


// PSEUDOCODE:

//DATA_SEGMENT
//var1 VAL 3
//var2 VAL 5
//result VAL 0
//CODE_SEGMENT
//LD var1
//MOVE BR, AR
//LD var2
//ADD
//PT result
//PRNTC result
//HALT
