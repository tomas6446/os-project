package org.os.userland;

import org.os.proc.Packet;
import org.os.proc.ProcessEnum;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ProcessManagerGUI extends JFrame {
    private final JTable processTable;
    private final DefaultTableModel processTableModel;
    private Map<ProcessEnum, Packet> processPacketMap = new HashMap<>();
    private final JTextField userInputBufferField;

    public ProcessManagerGUI() {
        setTitle("Process Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table for process statistics
        String[] columnNames = {"Process", "Packet Type", "Packet Data"};
        processTableModel = new DefaultTableModel(columnNames, 0);
        processTable = new JTable(processTableModel);
        add(new JScrollPane(processTable), BorderLayout.CENTER);

        // Text field for user input buffer
        userInputBufferField = new JTextField();
        userInputBufferField.setEditable(false);
        add(userInputBufferField, BorderLayout.SOUTH);

        // Initialize table with all possible processes
        for (ProcessEnum process : ProcessEnum.values()) {
            processTableModel.addRow(new Object[]{process, "", ""});
        }

        setVisible(true);
    }

    public void refreshTable(Map<ProcessEnum, Stack<Packet>> processes) {
        SwingUtilities.invokeLater(() -> {
            processPacketMap = new HashMap<>();
            processes.keySet().stream().filter(process -> !processes.get(process).isEmpty()).forEachOrdered(process -> {
                Packet packet = processes.get(process).peek();
                processPacketMap.put(process, packet);
            });
            refreshTable();
        });
    }

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            for (ProcessEnum process : processPacketMap.keySet()) {
                Packet packet = processPacketMap.get(process);
                if (packet != null) {
                    processTableModel.setValueAt(packet.getType(), process.ordinal(), 1);
                    processTableModel.setValueAt(packet.getData(), process.ordinal(), 2);
                } else {
                    processTableModel.setValueAt("", process.ordinal(), 1);
                    processTableModel.setValueAt("", process.ordinal(), 2);
                }
            }
        });
    }

    public void updateUserInputBuffer(String bufferContent) {
        SwingUtilities.invokeLater(() -> userInputBufferField.setText(bufferContent));
    }
}
