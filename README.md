# Virtual Machine Project

## Overview
This project is implementation of Virtual Machine (VM) in Java. It demonstrates the core concepts of how a VM operates, including the execution of instructions, handling of registers, memory management, and interaction with external devices.

## Features
- **Sample Program Execution**: Includes at least one sample program to demonstrate the VM's capabilities during the presentation.
- **Execution Modes**: Supports executing the program in either a step-by-step (debug) mode or running it directly.
- **User Interface**: Showcases command execution, the state changes of all VM components during step-by-step execution, including:
    - Register values
    - The next command to execute
    - External device states
    - VM page values of the executing command
- **Memory Visualization**: Allows displaying the VM's memory and the Real Machine's (RM) memory or a specified RM memory page.
- **Non-Sequential Page Allocation**: Implements non-sequential page allocation for the VM.

## Memory Management and Paging Mechanism

### Memory Layout

| Pages | 00  | 01  | 02  | 03  | 04  | 05  | 06  | 07  | 08  | 09  | 10  | 11  | 12  | 13  | 14  | 15  |
|-------|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|
| 00    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| 01    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| ...   | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... |
| 15    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| 16    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| ...   | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... |
| 272   |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
\
The rows represent the pages in memory, and the columns represent the memory cells within each page. Each page contains 16 memory cells.

### Memory Allocation
- The first 256 memory cells are reserved for page table entries (From 0 to 255).
- Virtual machines use cells from 256 to 272 for their operations (From 256 to 272).
- Real memory is allocated from cell 273 to 4368, providing ample space for virtual machine execution and data storage (From 273 to 4368).

### Address Translation Process
The process of converting a virtual address to a real address:

1. **Page Table Index Calculation**:
   The index of the page table is calculated using the formula: \
   Page Table Index = (PTR × 16) + (Address / 16), \
   where `PTR` is the page table register pointing to the page table in memory, and `Address` is the virtual address.

2. **Real Address Calculation**:
   The real address is calculated using the formula: \
   Real Address = (Page Number \times 16) + (Address \mod 16),\
   where:
- `Page Number` is obtained from the page table using the previously calculated index.
- `Address mod 16` indicates the byte number within the page.

## Program syntax
[The following program demonstrates the VM's capabilities by performing a simple addition operation](program_example.txt)
```assembly
DATA_SEGMENT
VAL 12 
VAL 3 
CODE_SEGMENT
MOVE AR,2
MOVE BR,3
ADD
JM 10
```

## Running the Project
```bash
git clone https://github.com/tomas6446/os-project.git
cd os-project
bash run.sh
```
