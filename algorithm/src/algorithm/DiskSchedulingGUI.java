package algorithm;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class DiskSchedulingGUI extends JFrame {
     private final ImageIcon icon;
    private final JTextField requestField = new JTextField(15);
    private final JTextField headField = new JTextField(15);
    private final JTextField rangeField = new JTextField(15);
    private final JComboBox<String> directionBox = new JComboBox<>(new String[]{"Left to Right", "Right to Left"});
    private final JTextArea outputArea = new JTextArea(20, 30);
    private JComboBox<String> algoBox = new JComboBox<>(new String[]{"FCFS", "SSTF", "SCAN", "LOOK", "C-SCAN", "C-LOOK"});

    public DiskSchedulingGUI() {
        setTitle("Disk Scheduling Algorithms");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.PINK);
        icon=new ImageIcon(getClass().getResource("image.jpg")); 
        this.setIconImage(icon.getImage());

        // Left Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(10, 1, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Inputs"));
        inputPanel.setBackground(new Color(255, 240, 240));

        inputPanel.add(new JLabel("Requests (e.g. 95,180,34):"));
        inputPanel.add(requestField);

        inputPanel.add(new JLabel("Initial Head Position:"));
        inputPanel.add(headField);

        inputPanel.add(new JLabel("Disk Range (0 to ?):"));
        inputPanel.add(rangeField);

        inputPanel.add(new JLabel("Direction:"));
        directionBox.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(directionBox);

        inputPanel.add(new JLabel("Select Algorithm:"));
        algoBox.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(algoBox);

        add(inputPanel, BorderLayout.WEST);

        // Right Output Panel
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        add(outputPanel, BorderLayout.CENTER);

        // Bottom Panel with Run Button
        JPanel bottomPanel = new JPanel();
        JButton runButton = new JButton("Run Algorithm");
        runButton.setFont(new Font("Arial", Font.BOLD, 16));
        runButton.setBackground(new Color(200, 255, 200));

        runButton.addActionListener(e -> {
            String algo = (String) algoBox.getSelectedItem();
            runAlgorithm(algo);
        });

        bottomPanel.add(runButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void runAlgorithm(String algo) {
        try {
            int[] requests = Arrays.stream(requestField.getText().split(","))
                    .mapToInt(s -> Integer.parseInt(s.trim())).toArray();
            int head = Integer.parseInt(headField.getText().trim());
            int range = Integer.parseInt(rangeField.getText().trim());
            String direction = (String) directionBox.getSelectedItem();

            switch (algo) {
                case "FCFS" -> fcfs(requests, head);
                case "SSTF" -> sstf(requests, head);
                case "SCAN" -> scan(requests, head, range, direction);
                case "LOOK" -> look(requests, head, direction);
                case "C-SCAN" -> cscan(requests, head, range);
                case "C-LOOK" -> clook(requests, head);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Input! Please check again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fcfs(int[] requests, int head) {
        StringBuilder movement = new StringBuilder();
        int total = 0;

        movement.append(head);
        for (int r : requests) {
            total += Math.abs(r - head);
            head = r;
            movement.append(" -> ").append(head);
        }

        outputArea.setText("Head Movement:\n" + movement + "\n\nTotal Seek Distance: " + total);
    }

    private void sstf(int[] requests, int head) {
        StringBuilder movement = new StringBuilder();
        boolean[] visited = new boolean[requests.length];
        int total = 0;

        movement.append(head);
        for (int i = 0; i < requests.length; i++) {
            int minDist = Integer.MAX_VALUE;
            int idx = -1;
            for (int j = 0; j < requests.length; j++) {
                if (!visited[j] && Math.abs(requests[j] - head) < minDist) {
                    minDist = Math.abs(requests[j] - head);
                    idx = j;
                }
            }
            visited[idx] = true;
            total += Math.abs(requests[idx] - head);
            head = requests[idx];
            movement.append(" -> ").append(head);
        }

        outputArea.setText("Head Movement:\n" + movement + "\n\nTotal Seek Distance: " + total);
    }

    private void scan(int[] requests, int head, int range, String direction) {
        Arrays.sort(requests);
        StringBuilder movement = new StringBuilder();
        int total = 0;
        int idx = 0;
        while (idx < requests.length && requests[idx] < head) idx++;

        movement.append(head);

        if (direction.equals("Left to Right")) {
            for (int i = idx; i < requests.length; i++) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
            if (head != range) {
                total += Math.abs(range - head);
                head = range;
                movement.append(" -> ").append(head);
            }
            for (int i = idx - 1; i >= 0; i--) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
        } else {
            for (int i = idx - 1; i >= 0; i--) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
            if (head != 0) {
                total += head;
                head = 0;
                movement.append(" -> ").append(head);
            }
            for (int i = idx; i < requests.length; i++) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
        }

        outputArea.setText("Head Movement:\n" + movement + "\n\nTotal Seek Distance: " + total);
    }

    private void look(int[] requests, int head, String direction) {
        Arrays.sort(requests);
        StringBuilder movement = new StringBuilder();
        int total = 0;
        int idx = 0;
        while (idx < requests.length && requests[idx] < head) idx++;

        movement.append(head);

        if (direction.equals("Left to Right")) {
            for (int i = idx; i < requests.length; i++) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
            for (int i = idx - 1; i >= 0; i--) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
        } else {
            for (int i = idx - 1; i >= 0; i--) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
            for (int i = idx; i < requests.length; i++) {
                total += Math.abs(requests[i] - head);
                head = requests[i];
                movement.append(" -> ").append(head);
            }
        }

        outputArea.setText("Head Movement:\n" + movement + "\n\nTotal Seek Distance: " + total);
    }

    private void cscan(int[] requests, int head, int range) {
        Arrays.sort(requests);
        StringBuilder movement = new StringBuilder();
        int total = 0;
        int idx = 0;
        while (idx < requests.length && requests[idx] < head) idx++;

        movement.append(head);

        for (int i = idx; i < requests.length; i++) {
            total += Math.abs(requests[i] - head);
            head = requests[i];
            movement.append(" -> ").append(head);
        }

        if (head != range) {
            total += Math.abs(range - head);
            head = 0;
            total += range;
            movement.append(" -> ").append(range).append(" -> 0");
        }

        for (int i = 0; i < idx; i++) {
            total += Math.abs(requests[i] - head);
            head = requests[i];
            movement.append(" -> ").append(head);
        }

        outputArea.setText("Head Movement:\n" + movement + "\n\nTotal Seek Distance: " + total);
    }

    private void clook(int[] requests, int head) {
        Arrays.sort(requests);
        StringBuilder movement = new StringBuilder();
        int total = 0;
        int idx = 0;
        while (idx < requests.length && requests[idx] < head) idx++;

        movement.append(head);

        for (int i = idx; i < requests.length; i++) {
            total += Math.abs(requests[i] - head);
            head = requests[i];
            movement.append(" -> ").append(head);
        }

        for (int i = 0; i < idx; i++) {
            total += Math.abs(requests[i] - head);
            head = requests[i];
            movement.append(" -> ").append(head);
        }

        outputArea.setText("Head Movement:\n" + movement + "\n\nTotal Seek Distance: " + total);
    }

    public static void main(String[] args) {
        new DiskSchedulingGUI();
    }
}
