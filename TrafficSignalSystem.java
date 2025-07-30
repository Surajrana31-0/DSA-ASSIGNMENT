// Import necessary Swing and AWT libraries
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Main class extending JFrame for GUI
public class TrafficSignalSystem extends JFrame {
    // Constant definitions for sizes and dimensions
    private static final int LANE_LENGTH = 150;
    private static final int VEHICLE_SIZE = 30;
    private static final int SIGNAL_SIZE = 40;
    private static final int LANE_WIDTH = 60;

    // Define color codes for lights, roads, and vehicles
    private static final Color RED_LIGHT = new Color(200, 0, 0);
    private static final Color GREEN_LIGHT = new Color(0, 180, 0);
    private static final Color YELLOW_LIGHT = new Color(220, 220, 0);
    private static final Color DARK_LIGHT = new Color(50, 50, 50);
    private static final Color ROAD_COLOR = new Color(80, 80, 80);
    private static final Color LANE_MARKER = new Color(200, 200, 200);
    private static final Color CAR_COLOR = new Color(30, 144, 255);
    private static final Color BUS_COLOR = new Color(220, 20, 60);
    private static final Color AMBULANCE_COLOR = new Color(255, 215, 0);
    private static final Color FIRE_TRUCK_COLOR = new Color(255, 69, 0);

    // GUI component declarations
    private JPanel intersectionPanel, controlPanel, queuePanel;
    private JButton addVehicleBtn, addEmergencyBtn, changeSignalBtn;
    private JLabel statusLabel;
    private DefaultListModel<String> queueModel;
    private JList<String> queueList;

    // Queues for regular and emergency vehicles
    private BlockingQueue<Vehicle> vehicleQueue = new LinkedBlockingQueue<>();
    private PriorityBlockingQueue<Vehicle> emergencyQueue = new PriorityBlockingQueue<>(10, 
        (v1, v2) -> {
            // Give Ambulance higher priority than Fire Truck
            if (v1.vehicleType.equals("Ambulance") && !v2.vehicleType.equals("Ambulance")) {
                return -1;
            } else if (!v1.vehicleType.equals("Ambulance") && v2.vehicleType.equals("Ambulance")) {
                return 1;
            }
            return 0;
        });

    // Thread pool and threads
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private TrafficLightThread trafficLightThread;
    private VehicleMovementThread vehicleMovementThread;
    private VehicleGeneratorThread vehicleGeneratorThread;

    // Enum and state tracking
    private enum Signal { NORTH_SOUTH, EAST_WEST }
    private Signal currentSignal = Signal.NORTH_SOUTH;
    private boolean northSouthGreen = true;
    private AtomicInteger vehiclesProcessed = new AtomicInteger(0);
    private AtomicInteger emergencyProcessed = new AtomicInteger(0);

    // Constructor to build the GUI and start background threads
    public TrafficSignalSystem() {
        setTitle("Traffic Signal Management System");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        // Intersection display area
        intersectionPanel = new IntersectionPanel();
        intersectionPanel.setPreferredSize(new Dimension(500, 500));
        intersectionPanel.setBorder(BorderFactory.createTitledBorder("Traffic Intersection"));

        // Control panel with buttons
        controlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(new Color(220, 220, 220));

        // Create and style control buttons
        addVehicleBtn = createStyledButton("Add Vehicle", new Color(70, 130, 180));
        addEmergencyBtn = createStyledButton("Add Emergency", new Color(220, 20, 60));
        changeSignalBtn = createStyledButton("Change Signal", new Color(46, 139, 87));

        // Signal status label
        statusLabel = new JLabel("Signal: NORTH-SOUTH Green", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(25, 25, 112));

        // Action listeners for control buttons
        addVehicleBtn.addActionListener(e -> addVehicle());
        addEmergencyBtn.addActionListener(e -> addEmergencyVehicle());
        changeSignalBtn.addActionListener(e -> changeSignal());

        // Add buttons and label to control panel
        controlPanel.add(addVehicleBtn);
        controlPanel.add(addEmergencyBtn);
        controlPanel.add(changeSignalBtn);
        controlPanel.add(statusLabel);

        // Queue display panel
        queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Vehicle Queue"));
        queuePanel.setBackground(new Color(240, 240, 240));

        // Queue model and list
        queueModel = new DefaultListModel<>();
        queueList = new JList<>(queueModel);
        queueList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        queueList.setBackground(new Color(250, 250, 250));
        JScrollPane queueScroll = new JScrollPane(queueList);

        // Add queue list to queue panel
        queuePanel.add(queueScroll, BorderLayout.CENTER);

        // Assemble main frame
        add(intersectionPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        add(queuePanel, BorderLayout.EAST);

        // Start background threads
        startThreads();
    }

    // Utility method to create styled buttons
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        return button;
    }

    // Start traffic control threads
    private void startThreads() {
        trafficLightThread = new TrafficLightThread();
        vehicleMovementThread = new VehicleMovementThread();
        vehicleGeneratorThread = new VehicleGeneratorThread();

        executor.execute(trafficLightThread);
        executor.execute(vehicleMovementThread);
        executor.execute(vehicleGeneratorThread);
    }

    // Add random regular vehicle to queue
    private void addVehicle() {
        String[] types = {"Car", "Bus"};
        String type = types[new Random().nextInt(types.length)];
        Vehicle vehicle = new Vehicle(type, false);
        vehicleQueue.add(vehicle);
        updateQueueDisplay();
    }

    // Add random emergency vehicle to priority queue
    private void addEmergencyVehicle() {
        String[] types = {"Ambulance", "Fire Truck"};
        String type = types[new Random().nextInt(types.length)];
        Vehicle vehicle = new Vehicle(type, true);
        emergencyQueue.add(vehicle);
        updateQueueDisplay();
    }

    // Toggle traffic signal between directions
    private void changeSignal() {
        currentSignal = (currentSignal == Signal.NORTH_SOUTH) ? Signal.EAST_WEST : Signal.NORTH_SOUTH;
        northSouthGreen = !northSouthGreen;
        statusLabel.setText("Signal: " + 
            (northSouthGreen ? "NORTH-SOUTH Green" : "EAST-WEST Green"));
        intersectionPanel.repaint();
    }

    // Update vehicle queue display on GUI
    private void updateQueueDisplay() {
        queueModel.clear();

        // Show emergency vehicles first
        for (Vehicle v : emergencyQueue) {
            queueModel.addElement(String.format("%-10s %s", v.vehicleType, "(EMERGENCY)"));
        }

        // Then regular vehicles
        for (Vehicle v : vehicleQueue) {
            queueModel.addElement(String.format("%-10s", v.vehicleType));
        }
    }

    // Class to define vehicle attributes
    private class Vehicle {
        String vehicleType;
        boolean isEmergency;
        int position;
        int lane;
        Color color;

        public Vehicle(String type, boolean emergency) {
            vehicleType = type;
            isEmergency = emergency;

            // Set color based on type
            switch (type) {
                case "Car": 
                    color = CAR_COLOR;
                    break;
                case "Bus": 
                    color = BUS_COLOR;
                    break;
                case "Ambulance": 
                    color = AMBULANCE_COLOR;
                    break;
                case "Fire Truck": 
                    color = FIRE_TRUCK_COLOR;
                    break;
            }
        }
    }

    // Panel that visually represents the intersection
    private class IntersectionPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;

            // Draw roads
            g.setColor(ROAD_COLOR);
            g.fillRect(centerX - LANE_WIDTH * 2, 0, LANE_WIDTH * 4, height);
            g.fillRect(0, centerY - LANE_WIDTH * 2, width, LANE_WIDTH * 4);

            // Draw dashed lane markers
            g.setColor(LANE_MARKER);
            for (int i = 0; i < 10; i++) {
                int y = centerY + i * 40 - 80;
                g.fillRect(centerX - 5, y, 10, 20);

                int x = centerX + i * 40 - 80;
                g.fillRect(x, centerY - 5, 20, 10);
            }

            // Draw all traffic lights
            drawTrafficSignal(g, centerX - 100, centerY - 150, true);
            drawTrafficSignal(g, centerX + 60, centerY - 150, true);
            drawTrafficSignal(g, centerX - 150, centerY - 100, false);
            drawTrafficSignal(g, centerX - 150, centerY + 60, false);
        }

        // Helper to draw a traffic signal box
        private void drawTrafficSignal(Graphics g, int x, int y, boolean vertical) {
            g.setColor(Color.BLACK);
            g.fillRect(x, y, SIGNAL_SIZE, SIGNAL_SIZE);

            // Draw green light for appropriate direction
            g.setColor(northSouthGreen && vertical ? GREEN_LIGHT : DARK_LIGHT);
            g.fillOval(x + 10, y + 5, 20, 20);

            g.setColor(!northSouthGreen && !vertical ? GREEN_LIGHT : DARK_LIGHT);
            g.fillOval(x + 10, y + 25, 20, 20);
        }
    }

    // Thread that switches signal every 10 seconds
    private class TrafficLightThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(10000);
                    SwingUtilities.invokeLater(() -> changeSignal());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Thread to move vehicles based on priority and queue
    private class VehicleMovementThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Vehicle vehicle = emergencyQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (vehicle != null) {
                        processVehicle(vehicle, true);
                        emergencyProcessed.incrementAndGet();
                    } else {
                        vehicle = vehicleQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (vehicle != null) {
                            processVehicle(vehicle, false);
                            vehiclesProcessed.incrementAndGet();
                        }
                    }

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Signal: " + 
                            (northSouthGreen ? "NORTH-SOUTH Green" : "EAST-WEST Green") +
                            " | Processed: " + vehiclesProcessed.get() + 
                            " | Emergency: " + emergencyProcessed.get());
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Simulate processing of a vehicle
        private void processVehicle(Vehicle vehicle, boolean emergency) {
            try {
                int processTime = emergency ? 1000 : 2000;
                Thread.sleep(processTime);

                SwingUtilities.invokeLater(() -> {
                    updateQueueDisplay();
                    intersectionPanel.repaint();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Thread to randomly generate vehicles
    private class VehicleGeneratorThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    int delay = 2000 + new Random().nextInt(3000);
                    Thread.sleep(delay);

                    if (new Random().nextInt(10) < 2) {
                        SwingUtilities.invokeLater(() -> addEmergencyVehicle());
                    } else {
                        SwingUtilities.invokeLater(() -> addVehicle());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Main method to run application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrafficSignalSystem system = new TrafficSignalSystem();
            system.setVisible(true);
            system.setLocationRelativeTo(null);
        });
    }
}
