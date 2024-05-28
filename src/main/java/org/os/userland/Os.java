package org.os.userland;

import org.os.core.*;
import org.os.proc.Planner;
import org.os.proc.ResourceManager;
import org.os.util.MemoryVisualiser;

import static java.lang.System.out;
import static org.os.userland.InteractiveInterface.REAL_MEMORY_SIZE;

public class Os {
    public Os(RealMachine realMachine) {
        out.println("=== Computer started ===");
        ResourceManager resourceManager = new ResourceManager();
        new Planner(realMachine, resourceManager).plan();
    }
}
