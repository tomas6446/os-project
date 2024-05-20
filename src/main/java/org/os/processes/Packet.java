package org.os.processes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Packet {
    WORK_END("work_end", ""),
    WORK_END_U_OFF("work_end_U", "off"),
    WORK_END_U_ADD("work_end_U", "add"),
    WORK_END_U_START("work_end_U", "start"),
    STOP("stop", ""),
    CONTINUE_WORK("continue_work", ""),
    END_OF_PROCESS("end_of_process", ""),
    START_NEW_PROCESS("start_new_process", ""),
    OUTPUT("output", ""),
    INPUT("input", ""),
    INPUT_I("input_I", ""),
    ADD_VM("add_VM", ""),
    NEW_VM_TO_LIST("new_VM_to_list", ""),
    ERROR("error", ""),
    ALL_DONE("all_done", ""),
    RUN_COMPLETE("run_complete", "");

    private final String name;
    private final String data;
}
