package Question5;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class SeatManager {
    private final ConcurrentHashMap<String, SeatStatus> seats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> seatBookedBy = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();

    public SeatManager(int rows, int cols) {
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String seatId = "R" + r + "C" + c;
                seats.put(seatId, SeatStatus.AVAILABLE);
                seatLocks.put(seatId, new ReentrantLock());
            }
        }
    }

    // Optimistic locking booking attempt, now also record bookedBy user
    public boolean bookSeatOptimistic(String seatId, String userId) {
        boolean booked = seats.compute(seatId, (key, status) -> {
            if (status == SeatStatus.AVAILABLE) {
                return SeatStatus.BOOKED;
            }
            return status;
        }) == SeatStatus.BOOKED;

        if (booked) {
            seatBookedBy.put(seatId, userId);
        }
        return booked;
    }

    // Pessimistic locking booking attempt
    public boolean bookSeatPessimistic(String seatId, String userId) {
        ReentrantLock lock = seatLocks.get(seatId);
        try {
            if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                try {
                    if (seats.get(seatId) == SeatStatus.AVAILABLE) {
                        seats.put(seatId, SeatStatus.BOOKED);
                        seatBookedBy.put(seatId, userId);
                        return true;
                    } else {
                        return false;
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                return false; // Timeout acquiring lock
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public SeatStatus getSeatStatus(String seatId) {
        return seats.get(seatId);
    }

    public String getSeatBookedBy(String seatId) {
        return seatBookedBy.get(seatId);
    }

    public ConcurrentHashMap<String, SeatStatus> getAllSeats() {
        return seats;
    }

    // For GUI: reset all seats and bookings (optional)
    public void resetSeats() {
        seats.replaceAll((k,v) -> SeatStatus.AVAILABLE);
        seatBookedBy.clear();
    }
}