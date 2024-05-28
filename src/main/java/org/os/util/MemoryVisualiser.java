package org.os.util;

import org.os.core.Cpu;
import org.os.core.Word;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MemoryVisualiser {
    private static final String[] memoryHeaders = new String[]{"Address", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"};
    private final Word[] memory;
    private JTable table;
    private DefaultTableModel tableModel;
    private final CpuVisualiser cpuVisualiser;

    private Font defaultFont = new Font("Serif", Font.PLAIN, 15);
    private int defaultRowHeight = 30;

    public MemoryVisualiser(Word[] memory, Cpu cpu) {
        this.memory = memory;
        this.cpuVisualiser = new CpuVisualiser(cpu);
        initializeUI();
        updateTable(toData(256, memory), memoryHeaders);
    }

    private static String[][] toData(int rows, Word[] memory) {
        String[][] data = new String[rows][17];

        for (int i = 0; i < rows; i++) {
            data[i][0] = String.format("%03d", i);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i][j + 1] = memory[address].getWord().toBinaryString();
            }
        }
        return data;
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Memory Visualiser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 600);

        tableModel = new DefaultTableModel(null, memoryHeaders);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel();
        JButton paginationButton = new JButton("Show Pagination");
        JButton virtualMachinesButton = new JButton("Show Virtual Machines");
        JButton virtualMemoryButton = new JButton("Show Virtual Memory");
        JButton fullMemoryButton = new JButton("Show Full Memory");
        JButton cpuRegistersButton = new JButton("Show CPU Registers");

        buttonPanel.add(paginationButton);
        buttonPanel.add(virtualMachinesButton);
        buttonPanel.add(virtualMemoryButton);
        buttonPanel.add(fullMemoryButton);
        buttonPanel.add(cpuRegistersButton);

        defaultFont();

        paginationButton.addActionListener(e -> {
            defaultFont();
            updateTable(toData(16, memory), memoryHeaders);
        });
        virtualMachinesButton.addActionListener(e -> {
            defaultFont();
            updateTable(showVirtualMachinesData(), memoryHeaders);
        });
        virtualMemoryButton.addActionListener(e -> {
            defaultFont();
            updateTable(showVirtualMemoryData(), memoryHeaders);
        });
        fullMemoryButton.addActionListener(e -> {
            defaultFont();
            updateTable(toData(256, memory), memoryHeaders);
        });
        cpuRegistersButton.addActionListener(e -> {
            updateTable(cpuVisualiser.getRegisterData(), cpuVisualiser.getCpuHeaders());
            setBiggerFont(20, 40);
        });

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void updateTable(String[][] data, String[] headers) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setDataVector(data, headers);
            tableModel.fireTableStructureChanged();
        });
    }

    public void setBiggerFont(int fontSize, int rowHeight) {
        table.setFont(new Font("Serif", Font.PLAIN, fontSize));
        table.setRowHeight(rowHeight);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, renderer);
    }

    public void defaultFont() {
        table.setFont(defaultFont);
        table.setRowHeight(defaultRowHeight);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.LEFT);
        table.setDefaultRenderer(Object.class, renderer);
    }

    public void setDefaultFont(Font font, int rowHeight) {
        this.defaultFont = font;
        this.defaultRowHeight = rowHeight;
    }

    public String[][] showVirtualMachinesData() {
        String[][] data = new String[1][17];

        data[0][0] = "016";
        for (int j = 0; j < 16; j++) {
            int address = 16 * 16 + j;
            data[0][j + 1] = memory[address].getWord().toBinaryString();
        }
        return data;
    }

    public String[][] showVirtualMemoryData() {
        String[][] data = new String[256][17];

        for (int i = 17; i < 273; i++) {
            data[i - 17][0] = String.format("%03d", i);
            for (int j = 0; j < 16; j++) {
                int address = i * 16 + j;
                data[i - 17][j + 1] = memory[address].getWord().toBinaryString();
            }
        }
        return data;
    }
}
