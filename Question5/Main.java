package Question5;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize components
            SeatManager seatManager = new SeatManager(10, 10); // 10x10 grid
            BookingProcessor processor = new BookingProcessor(seatManager);
            
            // Create and set up the window
            JFrame frame = new JFrame("Online Ticket Booking System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            
            // Add the booking panel
            BookingPanel bookingPanel = new BookingPanel(seatManager, processor);
            frame.add(bookingPanel);
            
            // Center and display the window
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // Add shutdown hook
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    processor.shutdown();
                    seatManager.shutdown();
                }
            });
        });
    }
}