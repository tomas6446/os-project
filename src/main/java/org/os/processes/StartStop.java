package org.os.processes;

import org.os.core.RealMachine;

public class StartStop {
    private final GetPutData getPutData;
    private final EnvironmentInteraction environmentInteraction;
    private final Interrupt interrupt;
    private final MainProcess mainProcess;
    private final Jcl jcl;
    private final Vm vm;

    public StartStop(RealMachine realMachine) {
        vm = new Vm(realMachine);
        getPutData = new GetPutData(realMachine);
        JobGovernor jobGovernor = new JobGovernor(getPutData);
        interrupt = new Interrupt(realMachine);
        environmentInteraction = new EnvironmentInteraction();
        mainProcess = new MainProcess(jobGovernor);
        jcl = new Jcl(realMachine, vm);

        process();
    }

    private void process() {
        Packet packet = Packet.ALL_DONE;

        while (true) {
            packet = environmentInteraction.interact(packet);
            packet = interrupt.interact(packet);

            /* Irenginio veikimo patikrinimas,
             * Reikiamu duomenu ikelimas i registra,
             * Reikiamu duomenu ikelimas i atminti */

            packet = mainProcess.interact(packet);
            packet = jcl.interact(packet);

            if (packet == Packet.WORK_END) {
                break;
            }
        }
        /* Reikiamu duomenu uzsaugojimas,
         * Proceso pabaiga */
    }
}
