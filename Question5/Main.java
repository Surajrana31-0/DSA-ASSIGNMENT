package Question5;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Online Ticket Booking System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);

            BookingPanel bookingPanel = new BookingPanel();
            frame.setContentPane(bookingPanel);

            frame.setVisible(true);
        });
    }
}