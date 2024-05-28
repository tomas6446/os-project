package org.os.util;

import com.jakewharton.fliptables.FlipTable;
import org.os.core.Word;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MemoryVisualiser {
    private static final String[] headers = new String[]{"Address", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"};
    private final Word[] memory;
    private JTable table;
    private DefaultTableModel tableModel;

    public MemoryVisualiser(Word[] memory) {
        this.memory = memory;
        initializeUI();
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Memory Visualiser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        tableModel = new DefaultTableModel(null, headers);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel();
        JButton paginationButton = new JButton("Show Pagination");
        JButton virtualMachinesButton = new JButton("Show Virtual Machines");
        JButton virtualMemoryButton = new JButton("Show Virtual Memory");
        JButton fullMemoryButton = new JButton("Show Full Memory");

        buttonPanel.add(paginationButton);
        buttonPanel.add(virtualMachinesButton);
        buttonPanel.add(virtualMemoryButton);
        buttonPanel.add(fullMemoryButton);

        paginationButton.addActionListener(e -> showPagination());

        virtualMachinesButton.addActionListener(e -> showVirtualMachines());

        virtualMemoryButton.addActionListener(e -> showVirtualMemory());

        fullMemoryButton.addActionListener(e -> showFullMemory());

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void updateTable(String[][] data) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // Clear existing data
            for (String[] row : data) {
                tableModel.addRow(row);
            }
        });
    }

    public void showPagination() {
        String[][] data = new String[16][17];

        for (int i = 0; i < 16; i++) {
            data[i][0] = String.format("%03d", i);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i][j + 1] = memory[address].getWord().toBinaryString();
            }
        }

        System.out.println("Pagination Table:");
        System.out.println(FlipTable.of(headers, data));
    }

    public void showVirtualMachines() {
        String[][] data = new String[1][17];

        data[0][0] = "016";
        for (int j = 0; j < 16; j++) {
            int address = 16 * 16 + j;
            data[0][j + 1] = memory[address].getWord().toBinaryString();
        }

        System.out.println("Virtual Machines:");
        System.out.println(FlipTable.of(headers, data));
    }

    public void showVirtualMemory() {
        String[][] data = new String[256][17];

        for (int i = 17; i < 273; i++) {
            data[i - 17][0] = String.format("%03d", i);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i - 17][j + 1] = memory[address].getWord().toBinaryString();
            }
        }

        System.out.println("Virtual Memory:");
        System.out.println(FlipTable.of(headers, data));
    }

    public void showFullMemory() {
        String[][] data = new String[256][17];

        for (int i = 0; i < 256; i++) {
            data[i][0] = String.format("%03d", i);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i][j + 1] = memory[address].getWord().toBinaryString();
            }
        }

        System.out.println("Full Memory:");
        System.out.println(FlipTable.of(headers, data));
    }
}
