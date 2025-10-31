package algorithm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BankersAlgorithmGUI extends JFrame {
     private final ImageIcon icon;

    private JTextArea outputArea;
    private JTextField allocationField, maxField, availableField;
    private JButton checkBtn;

    public BankersAlgorithmGUI() {
        setTitle("Banker's Algorithm");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.PINK);
        icon=new ImageIcon(getClass().getResource("image.jpg")); 
        this.setIconImage(icon.getImage());

        // Create panels
        JPanel inputPanel = new JPanel(null);
        JPanel outputPanel = new JPanel(null);

        inputPanel.setPreferredSize(new Dimension(400, 400));
        outputPanel.setPreferredSize(new Dimension(400, 400));

        // Input section
        JLabel allocationLabel = new JLabel("Allocation (rows with ; and values with ,):");
        allocationLabel.setBounds(20, 20, 360, 20);
        inputPanel.add(allocationLabel);

        allocationField = new JTextField();
        allocationField.setBounds(20, 45, 350, 25);
        inputPanel.add(allocationField);

        JLabel maxLabel = new JLabel("Max Matrix:");
        maxLabel.setBounds(20, 80, 360, 20);
        inputPanel.add(maxLabel);

        maxField = new JTextField();
        maxField.setBounds(20, 105, 350, 25);
        inputPanel.add(maxField);

        JLabel availableLabel = new JLabel("Available Resources:");
        availableLabel.setBounds(20, 140, 360, 20);
        inputPanel.add(availableLabel);

        availableField = new JTextField();
        availableField.setBounds(20, 165, 350, 25);
        inputPanel.add(availableField);

        checkBtn = new JButton("Check Safe Sequence");
        checkBtn.setBounds(100, 210, 200, 30);
        inputPanel.add(checkBtn);

        // Output section
        JLabel resultLabel = new JLabel("Result:");
        resultLabel.setBounds(20, 20, 100, 20);
        outputPanel.add(resultLabel);

        outputArea = new JTextArea();
        outputArea.setBounds(20, 50, 350, 250);
        outputArea.setEditable(false);
        outputArea.setBackground(Color.WHITE);
        outputArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        outputPanel.add(outputArea);

        // Add both panels
        add(inputPanel, BorderLayout.WEST);
        add(outputPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);
        setVisible(true);

        // Button event
        checkBtn.addActionListener(e -> checkSafeSequence());
    }

    private void checkSafeSequence() {
        try {
            // Parse Allocation matrix
            String[] allocationRows = allocationField.getText().trim().split(";");
            int[][] allocation = new int[allocationRows.length][];
            for (int i = 0; i < allocationRows.length; i++) {
                String[] nums = allocationRows[i].trim().split(",");
                allocation[i] = new int[nums.length];
                for (int j = 0; j < nums.length; j++) {
                    allocation[i][j] = Integer.parseInt(nums[j].trim());
                }
            }

            // Parse Max matrix
            String[] maxRows = maxField.getText().trim().split(";");
            int[][] max = new int[maxRows.length][];
            for (int i = 0; i < maxRows.length; i++) {
                String[] nums = maxRows[i].trim().split(",");
                max[i] = new int[nums.length];
                for (int j = 0; j < nums.length; j++) {
                    max[i][j] = Integer.parseInt(nums[j].trim());
                }
            }

            // Parse Available vector
            String[] availableNums = availableField.getText().trim().split(",");
            int[] available = new int[availableNums.length];
            for (int i = 0; i < availableNums.length; i++) {
                available[i] = Integer.parseInt(availableNums[i].trim());
            }

            String result = bankersAlgorithm(allocation, max, available);
            outputArea.setText(result);

        } catch (Exception ex) {
            outputArea.setText("Error in input format!\nUse , between numbers and ; between rows.");
        }
    }

    private String bankersAlgorithm(int[][] alloc, int[][] max, int[] avail) {
        int n = alloc.length;
        int m = avail.length;

        int[][] need = new int[n][m];
        boolean[] finish = new boolean[n];
        int[] safeSequence = new int[n];
        int count = 0;

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                need[i][j] = max[i][j] - alloc[i][j];

        int[] work = avail.clone();

        while (count < n) {
            boolean found = false;
            for (int i = 0; i < n; i++) {
                if (!finish[i]) {
                    boolean possible = true;
                    for (int j = 0; j < m; j++) {
                        if (need[i][j] > work[j]) {
                            possible = false;
                            break;
                        }
                    }
                    if (possible) {
                        for (int k = 0; k < m; k++) {
                            work[k] += alloc[i][k];
                        }
                        safeSequence[count++] = i;
                        finish[i] = true;
                        found = true;
                    }
                }
            }
            if (!found) {
                return "❌ System is NOT in a safe state!";
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("✅ System is in a safe state.\n");
        sb.append("Safe Sequence:\n");
        for (int i = 0; i < n; i++) {
            sb.append("P").append(safeSequence[i]);
            if (i != n - 1) sb.append(" -> ");
        }
        return sb.toString();
    }
}
