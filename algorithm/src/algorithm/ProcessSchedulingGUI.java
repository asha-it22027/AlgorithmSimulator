package algorithm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ProcessSchedulingGUI extends JFrame {
    private JTextField atField, btField, prField, qtField;
    private JTextArea outputArea;
    private JComboBox<String> algoBox;
    private final ImageIcon icon;

    public ProcessSchedulingGUI() {
        setTitle("Process Scheduling Simulator");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.PINK);
        icon=new ImageIcon(getClass().getResource("image.jpg")); 
        this.setIconImage(icon.getImage());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(7, 2, 5, 5));
        inputPanel.setBackground(Color.PINK);
        inputPanel.setPreferredSize(new Dimension(300, 500));

        inputPanel.add(new JLabel("Arrival Times (comma-separated):"));
        atField = new JTextField();
        inputPanel.add(atField);

        inputPanel.add(new JLabel("Burst Times (comma-separated):"));
        btField = new JTextField();
        inputPanel.add(btField);

        inputPanel.add(new JLabel("Priorities (comma-separated):"));
        prField = new JTextField();
        inputPanel.add(prField);

        inputPanel.add(new JLabel("Time Quantum (RR only):"));
        qtField = new JTextField();
        inputPanel.add(qtField);

        inputPanel.add(new JLabel("Select Algorithm:"));
        algoBox = new JComboBox<>(new String[]{
            "FCFS", "SJF (Non-Preemptive)", "SJF (Preemptive)",
            "Priority (Non-Preemptive)", "Priority (Preemptive)", "Round Robin"
        });
        inputPanel.add(algoBox);

        JButton runBtn = new JButton("Run Scheduling");
        inputPanel.add(runBtn);

        JPanel leftWrap = new JPanel(new BorderLayout());
        leftWrap.setBackground(Color.PINK);
        leftWrap.add(inputPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrap, scrollPane);
        splitPane.setDividerLocation(300);
        add(splitPane);

        runBtn.addActionListener(e -> schedule());

        setVisible(true);
    }

    static class Process {
        int id, at, bt, ct, wt, tat, rt, pr;
        Process(int id, int at, int bt, int pr) {
            this.id = id; this.at = at; this.bt = bt; this.pr = pr; this.rt = bt;
        }
    }

    private void schedule() {
        try {
            int[] at = Arrays.stream(atField.getText().split(",")).mapToInt(Integer::parseInt).toArray();
            int[] bt = Arrays.stream(btField.getText().split(",")).mapToInt(Integer::parseInt).toArray();
            int[] pr = prField.getText().isEmpty() ? new int[bt.length] : Arrays.stream(prField.getText().split(",")).mapToInt(Integer::parseInt).toArray();
            int quantum = qtField.getText().isEmpty() ? 1 : Integer.parseInt(qtField.getText().trim());
            String algo = (String) algoBox.getSelectedItem();

            List<Process> processes = new ArrayList<>();
            for (int i = 0; i < bt.length; i++) processes.add(new Process(i+1, at[i], bt[i], pr[i]));

            StringBuilder gantt = new StringBuilder("Gantt Chart:\n|");
            List<Integer> gList = new ArrayList<>();
            int time = 0, completed = 0;

            switch (algo) {
                case "FCFS":
                    processes.sort(Comparator.comparingInt(p -> p.at));
                    for (Process p : processes) {
                        time = Math.max(time, p.at);
                        p.wt = time - p.at;
                        time += p.bt;
                        p.ct = time;
                        p.tat = p.ct - p.at;
                        for (int i = 0; i < p.bt; i++) gList.add(p.id);
                    }
                    break;

                case "SJF (Non-Preemptive)":
                    processes.sort(Comparator.comparingInt(p -> p.at));
                    boolean[] done = new boolean[bt.length];
                    while (completed < bt.length) {
                        int minBt = Integer.MAX_VALUE, idx = -1;
                        for (int i = 0; i < processes.size(); i++) {
                            Process p = processes.get(i);
                            if (!done[i] && p.at <= time && p.bt < minBt) {
                                minBt = p.bt; idx = i;
                            }
                        }
                        if (idx == -1) { gList.add(-1); time++; continue; }
                        Process p = processes.get(idx);
                        time += p.bt;
                        p.ct = time;
                        p.tat = p.ct - p.at;
                        p.wt = p.tat - p.bt;
                        for (int i = 0; i < p.bt; i++) gList.add(p.id);
                        done[idx] = true; completed++;
                    }
                    break;

                case "SJF (Preemptive)":
                    while (completed < bt.length) {
                        Process minP = null;
                        for (Process p : processes)
                            if (p.at <= time && p.rt > 0 && (minP == null || p.rt < minP.rt))
                                minP = p;
                        if (minP == null) { gList.add(-1); time++; continue; }
                        minP.rt--; gList.add(minP.id);
                        if (minP.rt == 0) {
                            minP.ct = time + 1;
                            minP.tat = minP.ct - minP.at;
                            minP.wt = minP.tat - minP.bt;
                            completed++;
                        }
                        time++;
                    }
                    break;

                case "Priority (Non-Preemptive)":
                    processes.sort(Comparator.comparingInt(p -> p.at));
                    boolean[] visited = new boolean[bt.length];
                    while (completed < bt.length) {
                        Process minP = null;
                        for (int i = 0; i < processes.size(); i++) {
                            Process p = processes.get(i);
                            if (!visited[i] && p.at <= time && (minP == null || p.pr < minP.pr))
                                minP = p;
                        }
                        if (minP == null) { gList.add(-1); time++; continue; }
                        visited[processes.indexOf(minP)] = true;
                        time = Math.max(time, minP.at);
                        for (int i = 0; i < minP.bt; i++) gList.add(minP.id);
                        time += minP.bt;
                        minP.ct = time;
                        minP.tat = minP.ct - minP.at;
                        minP.wt = minP.tat - minP.bt;
                        completed++;
                    }
                    break;

                case "Priority (Preemptive)":
                    while (completed < bt.length) {
                        Process minP = null;
                        for (Process p : processes)
                            if (p.at <= time && p.rt > 0 && (minP == null || p.pr < minP.pr))
                                minP = p;
                        if (minP == null) { gList.add(-1); time++; continue; }
                        minP.rt--; gList.add(minP.id);
                        if (minP.rt == 0) {
                            minP.ct = time + 1;
                            minP.tat = minP.ct - minP.at;
                            minP.wt = minP.tat - minP.bt;
                            completed++;
                        }
                        time++;
                    }
                    break;

                case "Round Robin":
                    Queue<Process> queue = new LinkedList<>();
                    boolean[] enqueued = new boolean[bt.length];
                    while (completed < bt.length) {
                        for (int i = 0; i < processes.size(); i++)
                            if (processes.get(i).at <= time && !enqueued[i]) {
                                queue.add(processes.get(i));
                                enqueued[i] = true;
                            }
                        if (queue.isEmpty()) { gList.add(-1); time++; continue; }
                        Process p = queue.poll();
                        int ex = Math.min(p.rt, quantum);
                        for (int i = 0; i < ex; i++) gList.add(p.id);
                        p.rt -= ex;
                        time += ex;
                        for (int i = 0; i < processes.size(); i++)
                            if (processes.get(i).at <= time && !enqueued[i]) {
                                queue.add(processes.get(i));
                                enqueued[i] = true;
                            }
                        if (p.rt > 0) queue.add(p);
                        else {
                            p.ct = time;
                            p.tat = p.ct - p.at;
                            p.wt = p.tat - p.bt;
                            completed++;
                        }
                    }
                    break;
            }

            for (int id : gList)
                gantt.append(id == -1 ? " Idle |" : " P" + id + " |");

            StringBuilder result = new StringBuilder();
            result.append(gantt).append("\n\nP\tAT\tBT\tWT\tTAT\n");
            double totalWT = 0, totalTAT = 0;
            for (Process p : processes) {
                result.append("P").append(p.id).append("\t").append(p.at).append("\t").append(p.bt).append("\t").append(p.wt).append("\t").append(p.tat).append("\n");
                totalWT += p.wt;
                totalTAT += p.tat;
            }
            result.append("\nAverage WT: ").append(String.format("%.2f", totalWT / processes.size()));
            result.append("\nAverage TAT: ").append(String.format("%.2f", totalTAT / processes.size()));

            outputArea.setText(result.toString());
        } catch (Exception ex) {
            outputArea.setText("Invalid input! Please check and try again.");
        }
    }

    public static void main(String[] args) {
        new ProcessSchedulingGUI();
    }
}
