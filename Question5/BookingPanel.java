package Question5;

import javax.swing.Timer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

import java.util.*;


public class BookingPanel extends JPanel {
    private final int rows = 10;
    private final int cols = 10;
    private final SeatManager seatManager = new SeatManager(rows, cols);
    private final BlockingQueue<BookingRequest> bookingQueue = new LinkedBlockingQueue<>();
    private BookingProcessor bookingProcessor;

    private final JPanel seatGridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
    private final JTextArea logArea = new JTextArea(10, 40);
    private final DefaultListModel<String> queueListModel = new DefaultListModel<>();
    private final JList<String> queueList = new JList<>(queueListModel);

    // New booking history area
    private final JTextArea historyArea = new JTextArea(10, 40);

    private JRadioButton optimisticButton;
    private JRadioButton pessimisticButton;
    private JButton startButton;
    private JButton addRequestsButton;
    private JButton resetButton;
    private JButton cancelRequestButton;
    private JButton loginButton;
    private JButton pauseButton;
    private JButton resumeButton;

    private JLabel statsLabel = new JLabel("Success: 0 | Fail: 0");
    private JLabel userLabel = new JLabel("Not logged in");

    private ScheduledExecutorService refresher;

    private String currentUser = null;

    // For animation: Map seatId to current Color for smooth transitions
    private final Map<String, Color> seatColors = new HashMap<>();

    public BookingPanel() {
        setLayout(new BorderLayout(10, 10));
        initControls();
        initSeatGrid();
        initLoggingArea();
        initQueueList();
        initHistoryArea();
        updateSeatGrid();
        updateStatsLabel();

        // Prompt login at startup
        SwingUtilities.invokeLater(this::showLoginDialog);
    }

    private void initControls() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        optimisticButton = new JRadioButton("Optimistic Locking");
        pessimisticButton = new JRadioButton("Pessimistic Locking");
        ButtonGroup group = new ButtonGroup();
        group.add(optimisticButton);
        group.add(pessimisticButton);
        optimisticButton.setSelected(true);

        addRequestsButton = new JButton("Add Random Booking Requests");
        addRequestsButton.addActionListener(e -> addRandomBookingRequests());

        startButton = new JButton("Process Bookings");
        startButton.addActionListener(e -> startProcessing());

        resetButton = new JButton("Reset Seats");
        resetButton.addActionListener(e -> resetSystem());

        cancelRequestButton = new JButton("Cancel Selected Request");
        cancelRequestButton.addActionListener(e -> cancelSelectedRequest());

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> showLoginDialog());

        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> pauseProcessing());

        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e -> resumeProcessing());
        resumeButton.setEnabled(false);

        controlPanel.add(optimisticButton);
        controlPanel.add(pessimisticButton);
        controlPanel.add(addRequestsButton);
        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(cancelRequestButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resumeButton);
        controlPanel.add(loginButton);
        controlPanel.add(userLabel);
        controlPanel.add(statsLabel);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void initSeatGrid() {
        seatGridPanel.setBorder(new TitledBorder("Seat Availability"));
        add(seatGridPanel, BorderLayout.CENTER);
    }

    private void initLoggingArea() {
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Booking Logs"));
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void initQueueList() {
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(new TitledBorder("Pending Booking Requests"));
        queueList.setVisibleRowCount(10);
        queuePanel.add(new JScrollPane(queueList), BorderLayout.CENTER);
        add(queuePanel, BorderLayout.EAST);
    }

    private void initHistoryArea() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyArea.setEditable(false);
        historyArea.setBorder(new TitledBorder("Booking History"));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.WEST);
    }

    private void updateSeatGrid() {
        seatGridPanel.removeAll();
        ConcurrentHashMap<String, SeatStatus> seats = seatManager.getAllSeats();

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String seatId = "R" + r + "C" + c;
                SeatStatus status = seats.get(seatId);
                Color targetColor;

                switch (status) {
                    case AVAILABLE -> targetColor = Color.GREEN;
                    case BOOKED -> targetColor = Color.RED;
                    case LOCKED -> targetColor = Color.YELLOW;
                    default -> targetColor = Color.GRAY;
                }

                JButton seatBtn = new JButton(seatId);
                seatBtn.setFocusPainted(false);

                // Animate color transition
                Color currentColor = seatColors.getOrDefault(seatId, targetColor);
                if (!currentColor.equals(targetColor)) {
                    animateSeatColor(seatBtn, currentColor, targetColor, seatId);
                } else {
                    seatBtn.setBackground(targetColor);
                }
                seatColors.put(seatId, targetColor);

                seatBtn.setEnabled(status == SeatStatus.AVAILABLE);

                // Tooltip with status + bookedBy
                String bookedBy = seatManager.getSeatBookedBy(seatId);
                String tooltip = "Status: " + status;
                if (bookedBy != null) {
                    tooltip += " | Booked By: " + bookedBy;
                }
                seatBtn.setToolTipText(tooltip);

                // Hover effect
                seatBtn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (seatBtn.isEnabled()) seatBtn.setBackground(Color.CYAN);
                    }
                    public void mouseExited(MouseEvent e) {
                        seatBtn.setBackground(seatColors.get(seatId));
                    }
                });

                seatBtn.addActionListener(e -> addManualBooking(seatId));

                seatGridPanel.add(seatBtn);
            }
        }
        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    private void animateSeatColor(JButton btn, Color from, Color to, String seatId) {
        final int steps = 10;
        final int delay = 30; // ms
        final int[] count = {0};

        Timer timer = new Timer(delay, null);
        timer.addActionListener(e -> {
            float ratio = (float) count[0] / steps;
            int r = (int) (from.getRed() + ratio * (to.getRed() - from.getRed()));
            int g = (int) (from.getGreen() + ratio * (to.getGreen() - from.getGreen()));
            int b = (int) (from.getBlue() + ratio * (to.getBlue() - from.getBlue()));
            btn.setBackground(new Color(r, g, b));
            count[0]++;
            if (count[0] > steps) {
                btn.setBackground(to);
                seatColors.put(seatId, to);
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    private void addManualBooking(String seatId) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }

        if (seatManager.getSeatStatus(seatId) != SeatStatus.AVAILABLE) {
            JOptionPane.showMessageDialog(this, "Seat " + seatId + " is already booked or locked.");
            return;
        }

        BookingRequest request = new BookingRequest(currentUser, seatId);
        bookingQueue.offer(request);
        queueListModel.addElement(currentUser + " -> " + seatId);
        log("Added manual booking request: " + currentUser + " -> " + seatId);
    }

    private void addRandomBookingRequests() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }

        Random rand = new Random();
        int count = 10;
        for (int i = 0; i < count; i++) {
            int r = rand.nextInt(rows) + 1;
            int c = rand.nextInt(cols) + 1;
            String seatId = "R" + r + "C" + c;
            BookingRequest request = new BookingRequest(currentUser, seatId);
            bookingQueue.offer(request);
            queueListModel.addElement(currentUser + " -> " + seatId);
        }
        log(count + " random booking requests added for " + currentUser + ".");
    }

    private void startProcessing() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }

        if (bookingProcessor != null) {
            bookingProcessor.shutdown();
        }
        if (refresher != null && !refresher.isShutdown()) {
            refresher.shutdownNow();
        }

        boolean optimistic = optimisticButton.isSelected();
        bookingProcessor = new BookingProcessor(seatManager, bookingQueue, optimistic, 4, this::onBookingProcessed);
        bookingProcessor.processBookings();

        disableControlsWhileProcessing(true);

        refresher = Executors.newSingleThreadScheduledExecutor();
        refresher.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
            updateSeatGrid();
            updateQueueList();
            updateStatsLabel();
        }), 0, 1, TimeUnit.SECONDS);

        log("Started processing booking requests using " + (optimistic ? "Optimistic" : "Pessimistic") + " locking.");
    }

    private void onBookingProcessed(String message) {
        log(message);
        if (message.contains("succeeded")) {
            bookingHistoryAdd(message);
        }
    }

    private void bookingHistoryAdd(String entry) {
        SwingUtilities.invokeLater(() -> {
            historyArea.append(entry + "\n");
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
        });
    }

    private void updateQueueList() {
        queueListModel.clear();
        bookingQueue.forEach(req -> queueListModel.addElement(req.userId + " -> " + req.seatId));
    }

    private void cancelSelectedRequest() {
        int selected = queueList.getSelectedIndex();
        if (selected >= 0) {
            String selectedStr = queueListModel.getElementAt(selected);
            // Remove from queue by matching string user -> seatId
            // We must find the BookingRequest and remove from queue
            String[] parts = selectedStr.split(" -> ");
            if (parts.length == 2) {
                String userId = parts[0];
                String seatId = parts[1];

                // Remove the matching BookingRequest from bookingQueue
                bookingQueue.removeIf(req -> req.userId.equals(userId) && req.seatId.equals(seatId));
                queueListModel.remove(selected);
                log("Cancelled booking request: " + selectedStr);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a booking request to cancel.");
        }
    }

    private void updateStatsLabel() {
        if (bookingProcessor != null) {
            int success = bookingProcessor.getSuccessCount();
            int fail = bookingProcessor.getFailCount();
            statsLabel.setText("Success: " + success + " | Fail: " + fail);
        } else {
            statsLabel.setText("Success: 0 | Fail: 0");
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void resetSystem() {
        if (bookingProcessor != null) {
            bookingProcessor.shutdown();
            bookingProcessor = null;
        }
        if (refresher != null && !refresher.isShutdown()) {
            refresher.shutdownNow();
            refresher = null;
        }
        bookingQueue.clear();
        queueListModel.clear();
        seatManager.resetSeats();
        updateSeatGrid();
        logArea.setText("");
        historyArea.setText("");
        updateStatsLabel();
        disableControlsWhileProcessing(false);
        log("System reset.");
    }

    private void disableControlsWhileProcessing(boolean disable) {
        optimisticButton.setEnabled(!disable);
        pessimisticButton.setEnabled(!disable);
        addRequestsButton.setEnabled(!disable);
        startButton.setEnabled(!disable);
        resetButton.setEnabled(!disable);
        cancelRequestButton.setEnabled(!disable);
        loginButton.setEnabled(!disable);
        pauseButton.setEnabled(disable);
        resumeButton.setEnabled(!disable);
    }

    private void pauseProcessing() {
        if (bookingProcessor != null) {
            bookingProcessor.pause();
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(true);
        }
    }

    private void resumeProcessing() {
        if (bookingProcessor != null) {
            bookingProcessor.resume();
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
        }
    }

    private void showLoginDialog() {
        String user = JOptionPane.showInputDialog(this, "Enter your username:", "Login", JOptionPane.PLAIN_MESSAGE);
        if (user != null && !user.trim().isEmpty()) {
            currentUser = user.trim();
            userLabel.setText("Logged in as: " + currentUser);
            log("User logged in: " + currentUser);
        } else {
            JOptionPane.showMessageDialog(this, "Login is required to proceed.");
            showLoginDialog();
        }
    }
}