package org.os.processes;

import org.os.core.RealMachine;

public class StartStop {
    private final RealMachine realMachine;
    private final JobGovernor jobGovernor
    private final GetPutData getPutData;
    private final EnvironmentInteraction environmentInteraction;
    private final Interrupt interrupt;
    private final MainProcess mainProcess;
    private final Jcl jcl;
    private Vm vm;

    public StartStop(RealMachine realMachine) {
        this.realMachine = realMachine;

        vm = new Vm(realMachine);
        getPutData = new GetPutData();
        interrupt = new Interrupt(realMachine);
        environmentInteraction = new EnvironmentInteraction(getPutData);
        jobGovernor = new JobGovernor(getPutData);
        mainProcess = new MainProcess(realMachine, jobGovernor);
        jcl = new Jcl(realMachine, vm);

        process();
    }

    private void process() {
        Packet packet = null; // change this to a more meaningful value

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
