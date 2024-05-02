# Os project

## Overview
This project is implementation of Virtual Machine (VM) in Java.

## Memory Management and Paging Mechanism

### Memory Layout
The below table's rows represent the pages in memory, and the columns represent the memory cells within each page. Each page contains 16 memory cells.

| Pages | 00  | 01  | 02  | 03  | 04  | 05  | 06  | 07  | 08  | 09  | 10  | 11  | 12  | 13  | 14  | 15  |
|-------|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|
| 00    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| 01    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| ...   | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... |
| 15    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| 16    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |
| ...   | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... | ... |
| 272   |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |     |

### Memory Allocation
- The first 16 pages are reserved for page table entries (From cell 0 to 255).
- Virtual machines use 17th page to indicate that it is currently working (From cell 256 to 272).
- Real memory is from 18 to 272 page, providing ample space for virtual machine execution and data storage (From cell 273 to 4368).

### Address Translation Process
The process of converting a virtual address to a real address:

1. **Page Table Index Calculation**:
   The index of the page table is calculated using the formula: \
   Page Table Index = (PTR × 16) + (Address / 16), \
   where `PTR` is the page table register pointing to the page table in memory, and `Address` is the virtual address.

2. **Real Address Calculation**:
   The real address is calculated using the formula: \
   Real Address = (Page Number × 16) + (Address mod 16),\
   where:
- `Page Number` is obtained from the page table using the previously calculated index.
- `Address mod 16` indicates the byte number within the page.

## Program syntax
[Program example](program_example.txt)
```bash
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
