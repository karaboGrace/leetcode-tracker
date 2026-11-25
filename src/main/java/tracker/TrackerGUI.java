package tracker;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class TrackerGUI {

    private static final Path DATA_FILE = Path.of("data", "cards.csv");

    private final CardStore store = new CardStore(DATA_FILE);
    private List<LeetCodeCard> cards;
    private List<LeetCodeCard> displayedCards = new ArrayList<>();

    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JCheckBox dueOnlyCheckBox;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrackerGUI().createAndShow());
    }

    private void createAndShow() {
        cards = new ArrayList<>(store.load());

        frame = new JFrame("LeetCode Spaced Repetition Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 500);
        frame.setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Difficulty", "EF", "Reps", "Due Date", "Tags"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only grid; edits happen through the dialogs
            }
        };
        table = new JTable(model);
        table.setDefaultRenderer(Object.class, new DueRowRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleOpenUrl();
                }
            }
        });

        JButton addBtn = new JButton("Add Problem");
        addBtn.addActionListener(e -> handleAdd());

        JButton reviewBtn = new JButton("Review Selected");
        reviewBtn.addActionListener(e -> handleReview());

        JButton openBtn = new JButton("Open URL");
        openBtn.addActionListener(e -> handleOpenUrl());

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> handleDelete());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable());

        dueOnlyCheckBox = new JCheckBox("Due only");
        dueOnlyCheckBox.addActionListener(e -> refreshTable());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBtn);
        buttonPanel.add(reviewBtn);
        buttonPanel.add(openBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(dueOnlyCheckBox);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();
        frame.setVisible(true);
    }

    private void refreshTable() {
        model.setRowCount(0);
        boolean dueOnly = dueOnlyCheckBox != null && dueOnlyCheckBox.isSelected();

        displayedCards = cards.stream()
                .filter(c -> !dueOnly || c.isDue())
                .sorted(Comparator.comparing(LeetCodeCard::getDueDate))
                .collect(Collectors.toList());

        for (LeetCodeCard c : displayedCards) {
            model.addRow(new Object[]{
                    c.getId(),
                    c.getTitle(),
                    c.getDifficulty(),
                    String.format("%.2f", c.getEasinessFactor()),
                    c.getRepetitions(),
                    c.getDueDate(),
                    String.join(",", c.getTags())
            });
        }
    }

    private void handleAdd() {
        String title = JOptionPane.showInputDialog(frame, "Problem title:");
        if (title == null || title.isBlank()) return;

        String url = JOptionPane.showInputDialog(frame, "URL:");
        if (url == null) url = "";

        String[] difficulties = {"EASY", "MEDIUM", "HARD"};
        String difficultyStr = (String) JOptionPane.showInputDialog(
                frame, "Difficulty:", "Difficulty",
                JOptionPane.QUESTION_MESSAGE, null, difficulties, difficulties[0]);
        if (difficultyStr == null) return; // user hit Cancel

        String tagsStr = JOptionPane.showInputDialog(frame, "Tags (comma separated, optional):");
        List<String> tags = (tagsStr == null || tagsStr.isBlank())
                ? new ArrayList<>()
                : new ArrayList<>(List.of(tagsStr.split(",")));

        int nextId = cards.stream().mapToInt(LeetCodeCard::getId).max().orElse(0) + 1;
        LeetCodeCard card = new LeetCodeCard(nextId, title, url, Difficulty.fromString(difficultyStr), tags);

        cards.add(card);
        store.save(cards);
        refreshTable();
    }

    private void handleReview() {
        LeetCodeCard card = getSelectedCard();
        if (card == null) {
            JOptionPane.showMessageDialog(frame, "Select a problem in the table first.");
            return;
        }

        Integer[] qualities = {0, 1, 2, 3, 4, 5};
        Integer quality = (Integer) JOptionPane.showInputDialog(
                frame,
                "How well did you recall it?\n"
                        + "5 = instant/optimal  4 = solved, slower  3 = solved with a hint\n"
                        + "2 = failed, recognized it  1 = failed, vague  0 = blank",
                "Review: " + card.getTitle(),
                JOptionPane.QUESTION_MESSAGE, null, qualities, 4);
        if (quality == null) return;

        SM2Scheduler.review(card, quality);
        store.save(cards);
        refreshTable();
    }

    private void handleOpenUrl() {
        LeetCodeCard card = getSelectedCard();
        if (card == null || card.getUrl() == null || card.getUrl().isBlank()) {
            JOptionPane.showMessageDialog(frame, "No URL saved for this problem.");
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(card.getUrl()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Couldn't open URL: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        LeetCodeCard card = getSelectedCard();
        if (card == null) {
            JOptionPane.showMessageDialog(frame, "Select a problem in the table first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Delete \"" + card.getTitle() + "\"? This can't be undone.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        cards.remove(card);
        store.save(cards);
        refreshTable();
    }

    private LeetCodeCard getSelectedCard() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedCards.size()) return null;
        return displayedCards.get(row);
    }

    /** Highlights rows that are due today or overdue in light red. */
    private class DueRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                boolean due = row < displayedCards.size() && displayedCards.get(row).isDue();
                c.setBackground(due ? new Color(255, 220, 220) : Color.WHITE);
            }
            return c;
        }
    }
}