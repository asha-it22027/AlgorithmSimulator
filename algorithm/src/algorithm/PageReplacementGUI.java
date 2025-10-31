package algorithm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.ImageIcon;

public class PageReplacementGUI extends JFrame {
    private final ImageIcon icon;

    public PageReplacementGUI() {
    setTitle("Page Replacement Algorithms");
    setSize(500, 500);
    setLayout(new FlowLayout());
    getContentPane().setBackground(Color.PINK);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    icon = new ImageIcon(getClass().getResource("image.jpg")); 
    this.setIconImage(icon.getImage());

    JButton fifoBtn = new JButton("FIFO");
    JButton lruBtn = new JButton("LRU");
    JButton optimalBtn = new JButton("Optimal");

    add(fifoBtn);
    add(lruBtn);
    add(optimalBtn);

    fifoBtn.addActionListener(e -> runAlgorithm("FIFO"));
    lruBtn.addActionListener(e -> runAlgorithm("LRU"));
    optimalBtn.addActionListener(e -> runAlgorithm("Optimal"));

    setLocationRelativeTo(null); // ✅ This centers your frame
    setVisible(true);
}


    private void runAlgorithm(String type) {
        String pagesStr = JOptionPane.showInputDialog(this, "Enter page sequence (comma-separated):");
        String frameStr = JOptionPane.showInputDialog(this, "Enter number of frames:");

        if (pagesStr == null || frameStr == null || pagesStr.trim().isEmpty() || frameStr.trim().isEmpty()) return;

        try {
            String[] pagesArray = pagesStr.split(",");
            int[] pages = Arrays.stream(pagesArray).mapToInt(p -> Integer.parseInt(p.trim())).toArray();
            int frames = Integer.parseInt(frameStr.trim());

            Object[][] data;
            int pageFaults = 0;

            switch (type) {
                case "FIFO" -> {
                    Result fifoResult = fifo(pages, frames);
                    data = fifoResult.data;
                    pageFaults = fifoResult.faults;
                }
                case "LRU" -> {
                    Result lruResult = lru(pages, frames);
                    data = lruResult.data;
                    pageFaults = lruResult.faults;
                }
                case "Optimal" -> {
                    Result optResult = optimal(pages, frames);
                    data = optResult.data;
                    pageFaults = optResult.faults;
                }
                default -> {
                    return;
                }
            }

            showTable(data, frames, pageFaults, type + " Algorithm");

        } catch (NumberFormatException e) {
            showError("Invalid input. Please enter numbers only.");
        }
    }

    private Result fifo(int[] pages, int frames) {
        List<Integer> memory = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        List<Object[]> tableData = new ArrayList<>();
        int faults = 0;

        for (int page : pages) {
            boolean hit = memory.contains(page);
            if (!hit) {
                if (memory.size() < frames) {
                    memory.add(page);
                    queue.add(page);
                } else {
                    int removed = queue.poll();
                    memory.remove((Integer) removed);
                    memory.add(page);
                    queue.add(page);
                }
                faults++;
            }

            tableData.add(buildRow(page, memory, frames, hit));
        }

        return new Result(tableData.toArray(new Object[0][]), faults);
    }

    private Result lru(int[] pages, int frames) {
        LinkedHashMap<Integer, Integer> cache = new LinkedHashMap<>(frames, 0.75f, true);
        List<Object[]> tableData = new ArrayList<>();
        int faults = 0;

        for (int page : pages) {
            boolean hit = cache.containsKey(page);
            if (!hit) {
                if (cache.size() >= frames) {
                    int oldest = cache.keySet().iterator().next();
                    cache.remove(oldest);
                }
                cache.put(page, page);
                faults++;
            } else {
                cache.get(page); // make it most recently used
            }

            tableData.add(buildRow(page, new ArrayList<>(cache.keySet()), frames, hit));
        }

        return new Result(tableData.toArray(new Object[0][]), faults);
    }

    private Result optimal(int[] pages, int frames) {
        List<Integer> memory = new ArrayList<>();
        List<Object[]> tableData = new ArrayList<>();
        int faults = 0;

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            boolean hit = memory.contains(page);

            if (!hit) {
                if (memory.size() < frames) {
                    memory.add(page);
                } else {
                    int farthest = -1, indexToReplace = -1;

                    for (int j = 0; j < memory.size(); j++) {
                        int nextUse = Integer.MAX_VALUE;
                        for (int k = i + 1; k < pages.length; k++) {
                            if (pages[k] == memory.get(j)) {
                                nextUse = k;
                                break;
                            }
                        }
                        if (nextUse > farthest) {
                            farthest = nextUse;
                            indexToReplace = j;
                        }
                    }

                    memory.set(indexToReplace, page);
                }
                faults++;
            }

            tableData.add(buildRow(page, memory, frames, hit));
        }

        return new Result(tableData.toArray(new Object[0][]), faults);
    }

    private Object[] buildRow(int page, List<Integer> memory, int frames, boolean hit) {
        Object[] row = new Object[frames + 2];
        row[0] = page;
        for (int i = 0; i < frames; i++) {
            if (i < memory.size()) {
                row[i + 1] = memory.get(i);
            } else {
                row[i + 1] = "";
            }
        }
        row[frames + 1] = hit ? "Hit" : "Page Fault";
        return row;
    }

    private void showTable(Object[][] data, int frames, int faults, String title) {
        String[] columnNames = new String[frames + 2];
        columnNames[0] = "Page";
        for (int i = 1; i <= frames; i++) {
            columnNames[i] = "Frame " + i;
        }
        columnNames[frames + 1] = "Status";

        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(table);

        JLabel faultLabel = new JLabel("Total Page Faults = " + faults);
        faultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        faultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        faultLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(faultLabel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, title, true);
        dialog.getContentPane().add(panel);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static class Result {
        Object[][] data;
        int faults;

        Result(Object[][] data, int faults) {
            this.data = data;
            this.faults = faults;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PageReplacementGUI::new);
    }
}
