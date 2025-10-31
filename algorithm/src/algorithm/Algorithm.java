package algorithm;

import java.awt.Color;
import javax.swing.*;

public class Algorithm extends JFrame {
    private final ImageIcon icon;

    public Algorithm() {
        setTitle("Algorithm Simulator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.BLUE);
        
        icon = new ImageIcon(getClass().getResource("image.jpg")); 
        this.setIconImage(icon.getImage());

        JButton processBtn = new JButton("Process Scheduling");
        JButton pageBtn = new JButton("Page Replacement");
        JButton diskBtn = new JButton("Disk Scheduling");
        JButton bankerBtn = new JButton("Banker's Algorithm");

        processBtn.setBounds(100, 50, 300, 40);
        pageBtn.setBounds(100, 120, 300, 40);
        diskBtn.setBounds(100, 190, 300, 40);
        bankerBtn.setBounds(100, 260, 300, 40);

        add(processBtn);
        add(pageBtn);
        add(diskBtn);
        add(bankerBtn);

        // Open new windows on button click
        processBtn.addActionListener(e -> new ProcessSchedulingGUI());
        pageBtn.addActionListener(e -> new PageReplacementGUI());
        diskBtn.addActionListener(e -> new DiskSchedulingGUI());
        bankerBtn.addActionListener(e -> new BankersAlgorithmGUI());

        setLocationRelativeTo(null); // <-- This will center your window
        setVisible(true);
    }

    public static void main(String[] args) {
        new Algorithm();
    }
}
