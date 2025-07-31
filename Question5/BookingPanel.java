package Question5;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import java.util.*;

/**
 * A Swing panel that provides a graphical user interface for booking seats.
 * Allows users to view available seats, make bookings, and see booking history.
 */
public class BookingPanel extends JPanel {
    // Configuration constants
    private final int rows = 10;
    private final int cols = 10;
    
    // Core components
    private final SeatManager seatManager = new SeatManager(rows, cols);
    private final BlockingQueue<BookingRequest> bookingQueue = new LinkedBlockingQueue<>();
    private BookingProcessor bookingProcessor;
    
    // UI Components - Main panels
    private final JPanel seatGridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
    private final JTextArea logArea = new JTextArea(10, 40);
    private final DefaultListModel<String> queueListModel = new DefaultListModel<>();
    private final JList<String> queueList = new JList<>(queueListModel);
    private final JTextArea historyArea = new JTextArea(10, 40);
    
    // UI Components - Controls
    private JRadioButton optimisticButton;
    private JRadioButton pessimisticButton;
    private JButton startButton;
    private JButton addRequestsButton;
    private JButton resetButton;
    private JButton cancelRequestButton;
    private JButton loginButton;
    private JButton pauseButton;
    private JButton resumeButton;
    
    // UI Components - Labels
    private JLabel statsLabel = new JLabel("Success: 0 | Fail: 0");
    private JLabel userLabel = new JLabel("Not logged in");
    
    // Background services
    private ScheduledExecutorService refresher;
    private String currentUser = null;
    
    // Visual state tracking
    private final Map<String, Color> seatColors = new HashMap<>();
    
    // Custom color scheme for modern look
    private final Color primaryColor = new Color(63, 81, 181);
    private final Color secondaryColor = new Color(233, 30, 99);
    private final Color accentColor = new Color(255, 193, 7);
    private final Color darkColor = new Color(33, 33, 33);
    private final Color lightColor = new Color(250, 250, 250);

    /**
     * Creates new BookingPanel with initialized UI components
     */
    public BookingPanel() {
        // Set up panel basics
        setLayout(new BorderLayout(10, 10));
        setBackground(lightColor);
        
        // Initialize and arrange all UI components
        initControls();
        initSeatGrid();
        initLoggingArea();
        initQueueList();
        initHistoryArea();
        
        // Update initial state
        updateSeatGrid();
        updateStatsLabel();
        applyModernTheme();

        // Prompt login dialog on startup
        SwingUtilities.invokeLater(this::showLoginDialog);
    }

    /**
     * Applies modern material design inspired theme to components
     */
    private void applyModernTheme() {
        // Apply styling to all buttons
        for (Component c : getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton)c;
                b.setBackground(primaryColor);
                b.setForeground(Color.WHITE);
                b.setOpaque(true);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            else if (c instanceof JRadioButton) {
                JRadioButton rb = (JRadioButton)c;
                rb.setForeground(darkColor);
            }
        }
        
        // Style text areas
        logArea.setBackground(new Color(245, 245, 245));
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyArea.setBackground(new Color(245, 245, 245));
        historyArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Style list
        queueList.setBackground(lightColor);
        queueList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Style labels
        userLabel.setForeground(secondaryColor);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statsLabel.setForeground(darkColor);
    }

    /**
     * Initializes all control buttons and radio buttons
     */
    private void initControls() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setBackground(lightColor);
        
        // Create locking mechanism selection
        optimisticButton = new JRadioButton("Optimistic Locking");
        pessimisticButton = new JRadioButton("Pessimistic Locking");
        ButtonGroup group = new ButtonGroup();
        group.add(optimisticButton);
        group.add(pessimisticButton);
        optimisticButton.setSelected(true);

        // Initialize all buttons with their actions
        addRequestsButton = new JButton("Add Random Booking Requests");
        addRequestsButton.addActionListener(e -> addRandomBookingRequests());
        
        startButton = new JButton("Process Bookings");
        startButton.addActionListener(e -> startProcessing());
        
        resetButton = new JButton("Reset System");
        resetButton.addActionListener(e -> resetSystem());
        
        cancelRequestButton = new JButton("Cancel Request");
        cancelRequestButton.addActionListener(e -> cancelSelectedRequest());
        
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> showLoginDialog());
        
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> pauseProcessing());
        
        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e -> resumeProcessing());
        resumeButton.setEnabled(false);
        
        // Add components to control panel
        controlPanel.add(optimisticButton);
        controlPanel.add(pessimisticButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(addRequestsButton);
        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(cancelRequestButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(pauseButton);
        controlPanel.add(resumeButton);
        controlPanel.add(loginButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(userLabel);
        controlPanel.add(statsLabel);
        
        // Add control panel to main panel
        add(controlPanel, BorderLayout.NORTH);
    }

    /**
     * Initializes the seat grid panel with empty buttons
     */
    private void initSeatGrid() {
        seatGridPanel.setBorder(new TitledBorder("Seat Availability"));
        seatGridPanel.setBackground(lightColor);
        add(seatGridPanel, BorderLayout.CENTER);
    }

    /**
     * Initializes the logging area with scroll pane
     */
    private void initLoggingArea() {
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Booking Logs"));
        add(scrollPane, BorderLayout.SOUTH);
    }

    /**
     * Initializes the pending requests queue list
     */
    private void initQueueList() {
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(new TitledBorder("Pending Booking Requests"));
        queuePanel.setBackground(lightColor);
        queueList.setVisibleRowCount(10);
        queuePanel.add(new JScrollPane(queueList), BorderLayout.CENTER);
        add(queuePanel, BorderLayout.EAST);
    }

    /**
     * Initializes the booking history area
     */
    private void initHistoryArea() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(lightColor);
        historyArea.setEditable(false);
        historyArea.setBorder(new TitledBorder("Booking History"));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.WEST);
    }

    /**
     * Updates the seat grid based on current seat status
     */
    private void updateSeatGrid() {
        seatGridPanel.removeAll();
        ConcurrentHashMap<String, SeatStatus> seats = seatManager.getAllSeats();

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String seatId = "R" + r + "C" + c;
                SeatStatus status = seats.get(seatId);
                
                // Determine seat color based on status
                Color targetColor;
                switch (status) {
                    case AVAILABLE: 
                        targetColor = new Color(67, 160, 71); // Green
                        break;
                    case BOOKED: 
                        targetColor = new Color(229, 57, 53); // Red 
                        break;
                    case LOCKED: 
                        targetColor = new Color(255, 235, 59); // Yellow
                        break;
                    default: 
                        targetColor = Color.GRAY;
                }

                JButton seatBtn = createSeatButton(seatId, targetColor, status);
                seatGridPanel.add(seatBtn);
            }
        }
        
        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    /**
     * Creates and configures a seat button with proper styling
     */
    private JButton createSeatButton(String seatId, Color targetColor, SeatStatus status) {
        JButton btn = new JButton(seatId);
        btn.setBackground(targetColor);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        // Only enable button if seat is available
        btn.setEnabled(status == SeatStatus.AVAILABLE);
        
        // Set tooltip with status info
        String bookedBy = seatManager.getSeatBookedBy(seatId);
        String tooltip = "Status: " + status;
        if (bookedBy != null) {
            tooltip += "\nBooked By: " + bookedBy;
        }
        btn.setToolTipText(tooltip);

        // Hover effects
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(targetColor.brighter());
                }
            }
            
            public void mouseExited(MouseEvent e) {
                btn.setBackground(targetColor);
            }
        });

        // Click handler for manual booking
        btn.addActionListener(e -> addManualBooking(seatId));
        
        return btn;
    }

    /**
     * Adds a manual booking request for the specified seat
     */
    private void addManualBooking(String seatId) {
        if (currentUser == null) {
            showLoginDialog();
            return;
        }

        SeatStatus status = seatManager.getSeatStatus(seatId);
        if (status != SeatStatus.AVAILABLE) {
            JOptionPane.showMessageDialog(this, 
                "Seat " + seatId + " is currently " + status.toString().toLowerCase(),
                "Booking Failed", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookingRequest request = new BookingRequest(currentUser, seatId);
        bookingQueue.offer(request);
        queueListModel.addElement(currentUser + " -> " + seatId);
        log("Added manual booking request: " + currentUser + " -> " + seatId);
    }

    /**
     * Adds several random booking requests for demo purposes
     */
    private void addRandomBookingRequests() {
        if (currentUser == null) {
            showLoginDialog();
            return;
        }

        Random rand = new Random();
        int count = rand.nextInt(5) + 5; // Add 5-10 random requests
        
        for (int i = 0; i < count; i++) {
            String seatId = "R" + (rand.nextInt(rows) + 1) + "C" + (rand.nextInt(cols) + 1);
            
            if (seatManager.getSeatStatus(seatId) == SeatStatus.AVAILABLE) {
                BookingRequest request = new BookingRequest(currentUser, seatId);
                bookingQueue.offer(request);
                queueListModel.addElement(currentUser + " -> " + seatId);
            }
        }
        
        log("Added " + count + " random booking requests for " + currentUser);
    }

    /**
     * Starts processing booking requests in the queue
     */
    private void startProcessing() {
        if (currentUser == null) {
            showLoginDialog();
            return;
        }

        // Clean up any existing processors
        if (bookingProcessor != null) {
            bookingProcessor.shutdown();
        }
        
        if (refresher != null && !refresher.isShutdown()) {
            refresher.shutdownNow();
        }

        // Create new processor with selected strategy
        boolean optimistic = optimisticButton.isSelected();
        bookingProcessor = new BookingProcessor(
            seatManager, 
            bookingQueue, 
            optimistic, 
            4, 
            this::onBookingProcessed
        );
        
        bookingProcessor.processBookings();
        disableControlsWhileProcessing(true);

        // Set up periodic UI refresher
        refresher = Executors.newSingleThreadScheduledExecutor();
        refresher.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                updateSeatGrid();
                updateQueueList();
                updateStatsLabel();
            });
        }, 0, 1, TimeUnit.SECONDS);

        log("Started processing with " + (optimistic ? "optimistic" : "pessimistic") + " locking");
    }

    /**
     * Callback for booking completion events
     */
    private void onBookingProcessed(String message) {
        log(message);
        if (message.contains("succeeded")) {
            bookingHistoryAdd(message);
        }
    }

    /**
     * Adds an entry to the booking history
     */
    private void bookingHistoryAdd(String entry) {
        SwingUtilities.invokeLater(() -> {
            historyArea.append(entry + "\n");
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
        });
    }

    /**
     * Updates the pending requests queue display
     */
    private void updateQueueList() {
        queueListModel.clear();
        bookingQueue.forEach(req -> 
            queueListModel.addElement(req.userId + " -> " + req.seatId)
        );
    }

    /**
     * Updates the success/failure statistics display
     */
    private void updateStatsLabel() {
        if (bookingProcessor != null) {
            statsLabel.setText(String.format(
                "Success: %d | Fail: %d", 
                bookingProcessor.getSuccessCount(),
                bookingProcessor.getFailCount()
            ));
        } else {
            statsLabel.setText("Success: 0 | Fail: 0");
        }
    }

    /**
     * Logs a message to the log area
     */
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new Date() + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Resets the entire booking system to initial state
     */
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
        updateQueueList();
        updateStatsLabel();
        
        logArea.setText("");
        historyArea.setText("");
        
        disableControlsWhileProcessing(false);
        log("System has been reset");
    }

    /**
     * Enables/disables controls based on processing state
     */
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

    /**
     * Pauses booking processing
     */
    private void pauseProcessing() {
        if (bookingProcessor != null) {
            bookingProcessor.pause();
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(true);
            log("Processing paused");
        }
    }

    /**
     * Resumes paused booking processing
     */
    private void resumeProcessing() {
        if (bookingProcessor != null) {
            bookingProcessor.resume();
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
            log("Processing resumed");
        }
    }

    /**
     * Shows login dialog to get username
     */
    private void showLoginDialog() {
        String username = JOptionPane.showInputDialog(
            this,
            "Enter your username:",
            "Login Required",
            JOptionPane.PLAIN_MESSAGE
        );

        if (username != null && !username.trim().isEmpty()) {
            currentUser = username.trim();
            userLabel.setText("User: " + currentUser);
            log("Logged in as: " + currentUser);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "You must login to use the booking system",
                "Login Required", 
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Cancels the selected booking request from the queue
     */
    private void cancelSelectedRequest() {
        int selectedIndex = queueList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < queueListModel.size()) {
            String selectedValue = queueListModel.getElementAt(selectedIndex);
            // Find and remove the corresponding BookingRequest from the queue
            BookingRequest toRemove = null;
            for (BookingRequest req : bookingQueue) {
                String display = req.userId + " -> " + req.seatId;
                if (display.equals(selectedValue)) {
                    toRemove = req;
                    break;
                }
            }
            if (toRemove != null) {
                bookingQueue.remove(toRemove);
                queueListModel.remove(selectedIndex);
                log("Cancelled booking request: " + selectedValue);
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Please select a booking request to cancel.",
                "Cancel Request",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
