package Question5;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// ...existing code...
public class BookingPanel extends JPanel {
    private static final int SEAT_SIZE = 50;
    private static final Color AVAILABLE_COLOR = new Color(144, 238, 144);
    private static final Color BOOKED_COLOR = new Color(220, 20, 60);
    private static final Color PROCESSING_COLOR = new Color(255, 215, 0);
    private static final Color SELECTED_COLOR = new Color(70, 130, 180);

    private final SeatManager seatManager;
    private final BookingProcessor processor;
    private final JTextArea logArea;
    private final JLabel statsLabel;
    private final Map<String, JButton> seatButtons = new HashMap<>();
    private final Random random = new Random();

    public BookingPanel(SeatManager seatManager, BookingProcessor processor) {
        this.seatManager = seatManager;
        this.processor = processor;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create seat panel
        JPanel seatPanel = createSeatPanel();
        JScrollPane seatScroll = new JScrollPane(seatPanel);
        seatScroll.setBorder(BorderFactory.createTitledBorder("Seating Chart"));

        // Create control panel
        JPanel controlPanel = createControlPanel();

        // Create log panel
        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Booking Log"));

        // Create stats panel
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        updateStats();
        JPanel statsPanel = new JPanel();
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statsPanel.add(statsLabel);

        // Add components
        add(seatScroll, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        add(logScroll, BorderLayout.EAST);
        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createSeatPanel() {
        JPanel seatPanel = new JPanel(new GridLayout(0, 10, 5, 5));

        for (String seatId : seatManager.getAllSeats()) {
            JButton seatButton = new JButton(seatId);
            seatButton.setOpaque(true);
            seatButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            seatButton.setFont(new Font("Arial", Font.BOLD, 10));
            updateSeatAppearance(seatButton, seatManager.getSeatStatus(seatId));
            seatButtons.put(seatId, seatButton);

            seatButton.addActionListener(e -> {
                String userId = "User" + (random.nextInt(900) + 100);
                seatManager.addBookingRequest(userId, seatId);
                log("Booking request added: " + userId + " -> " + seatId);
                seatButton.setBackground(PROCESSING_COLOR);

                // Reset color after delay
                Timer timer = new Timer(500, evt -> refreshSeatAppearances());
                timer.setRepeats(false);
                timer.start();

                updateStats();
            });

            seatPanel.add(seatButton);
        }

        return seatPanel;
    }

    private void refreshSeatAppearances() {
        SwingUtilities.invokeLater(() -> {
            for (Map.Entry<String, JButton> entry : seatButtons.entrySet()) {
                updateSeatAppearance(entry.getValue(), seatManager.getSeatStatus(entry.getKey()));
            }
        });
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 5, 10, 10));

        JButton addRequestBtn = createStyledButton("Add Random Request", new Color(70, 130, 180));
        JButton processBtn = createStyledButton("Start Processing", new Color(46, 139, 87));
        JButton stopBtn = createStyledButton("Stop Processing", new Color(178, 34, 34));
        JButton cancelBtn = createStyledButton("Cancel Random", new Color(148, 0, 211));

        JRadioButton optimisticBtn = new JRadioButton("Optimistic", true);
        JRadioButton pessimisticBtn = new JRadioButton("Pessimistic");

        ButtonGroup group = new ButtonGroup();
        group.add(optimisticBtn);
        group.add(pessimisticBtn);

        optimisticBtn.addActionListener(e -> processor.setOptimisticMode(true));
        pessimisticBtn.addActionListener(e -> processor.setOptimisticMode(false));

        addRequestBtn.addActionListener(e -> {
            addRandomBooking();
            updateStats();
        });

        processBtn.addActionListener(e -> {
            processor.startProcessing();
            log("Processing started with " + (processor.isOptimisticMode() ? "optimistic" : "pessimistic") + " locking");
        });

        stopBtn.addActionListener(e -> {
            processor.stopProcessing();
            log("Processing stopped");
        });

        cancelBtn.addActionListener(e -> {
            seatManager.cancelRandomBooking();
            log("Random booking cancelled");
            refreshSeatAppearances();
            updateStats();
        });

        JPanel modePanel = new JPanel(new GridLayout(2, 1));
        modePanel.add(optimisticBtn);
        modePanel.add(pessimisticBtn);

        controlPanel.add(addRequestBtn);
        controlPanel.add(processBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(cancelBtn);
        controlPanel.add(modePanel);

        return controlPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        return button;
    }

    private void addRandomBooking() {
        List<String> seats = new ArrayList<>(seatManager.getAllSeats());
        if (seats.isEmpty()) return;

        String seatId = seats.get(random.nextInt(seats.size()));
        String userId = "User" + (random.nextInt(900) + 100);

        seatManager.addBookingRequest(userId, seatId);
        log("Random booking added: " + userId + " -> " + seatId);
        refreshSeatAppearances();
    }

    private void updateSeatAppearance(JButton button, SeatStatus status) {
        switch (status) {
            case AVAILABLE:
                button.setBackground(AVAILABLE_COLOR);
                break;
            case PROCESSING:
                button.setBackground(PROCESSING_COLOR);
                break;
            case BOOKED:
                button.setBackground(BOOKED_COLOR);
                break;
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new Date() + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateStats() {
        SwingUtilities.invokeLater(() -> {
            String stats = String.format("<html>Total: %d | Success: %d | Failed: %d<br>Retries: %d | Cancelled: %d | Queue: %d</html>",
                    seatManager.getTotalBookings(),
                    seatManager.getSuccessfulBookings(),
                    seatManager.getFailedBookings(),
                    seatManager.getRetryCount(),
                    seatManager.getCancellationCount(),
                    seatManager.getQueueSize());
            statsLabel.setText(stats);
        });
    }
}
// ...existing code...