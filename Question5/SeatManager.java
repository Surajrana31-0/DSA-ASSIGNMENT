package Question5;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

public class SeatManager {
    private final Map<String, SeatStatus> seatStatus = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> seatVersions = new ConcurrentHashMap<>();
    private final Map<String, Lock> seatLocks = new ConcurrentHashMap<>();
    private final Queue<BookingRequest> bookingQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    // Statistics
    private final AtomicInteger totalBookings = new AtomicInteger(0);
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final AtomicInteger cancellationCount = new AtomicInteger(0);
    
    public SeatManager(int rows, int cols) {
        initializeSeats(rows, cols);
    }
    
    private void initializeSeats(int rows, int cols) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String seatId = String.format("%c%d", 'A' + row, col + 1);
                seatStatus.put(seatId, SeatStatus.AVAILABLE);
                seatVersions.put(seatId, new AtomicInteger(0));
                seatLocks.put(seatId, new ReentrantLock());
            }
        }
    }
    
    public SeatStatus getSeatStatus(String seatId) {
        return seatStatus.getOrDefault(seatId, SeatStatus.AVAILABLE);
    }
    
    public void addBookingRequest(String userId, String seatId) {
        bookingQueue.add(new BookingRequest(userId, seatId));
    }
    
    public void processBookings(boolean optimisticMode) {
        while (!bookingQueue.isEmpty()) {
            BookingRequest request = bookingQueue.poll();
            executor.submit(() -> processBooking(request, optimisticMode));
        }
    }
    
    private void processBooking(BookingRequest request, boolean optimisticMode) {
        totalBookings.incrementAndGet();
        boolean success;
        
        if (optimisticMode) {
            success = bookWithOptimisticLocking(request);
        } else {
            success = bookWithPessimisticLocking(request);
        }
        
        if (success) {
            successfulBookings.incrementAndGet();
        } else {
            failedBookings.incrementAndGet();
        }
    }
    
    private boolean bookWithOptimisticLocking(BookingRequest request) {
        String seatId = request.getSeatId();
        AtomicInteger version = seatVersions.get(seatId);
        int currentVersion = version.get();
        
        // Check availability
        if (seatStatus.get(seatId) != SeatStatus.AVAILABLE) {
            return false;
        }
        
        // Simulate processing time
        try {
            Thread.sleep(new Random().nextInt(50) + 30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Attempt to book with version check
        synchronized (seatStatus) {
            if (seatStatus.get(seatId) == SeatStatus.AVAILABLE && 
                version.get() == currentVersion) {
                seatStatus.put(seatId, SeatStatus.BOOKED);
                version.incrementAndGet();
                return true;
            }
        }
        
        // Conflict detected
        retryCount.incrementAndGet();
        return false;
    }
    
    private boolean bookWithPessimisticLocking(BookingRequest request) {
        String seatId = request.getSeatId();
        Lock lock = seatLocks.get(seatId);
        
        try {
            // Acquire lock with timeout to prevent deadlocks
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // Check availability
                    if (seatStatus.get(seatId) != SeatStatus.AVAILABLE) {
                        return false;
                    }
                    
                    // Mark as processing
                    seatStatus.put(seatId, SeatStatus.PROCESSING);
                    
                    // Simulate processing time
                    try {
                        Thread.sleep(new Random().nextInt(50) + 30);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Book the seat
                    seatStatus.put(seatId, SeatStatus.BOOKED);
                    return true;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Lock acquisition failed
        retryCount.incrementAndGet();
        return false;
    }
    
    public void cancelBooking(String seatId) {
        if (seatStatus.containsKey(seatId)) {
            seatStatus.put(seatId, SeatStatus.AVAILABLE);
            cancellationCount.incrementAndGet();
        }
    }
    
    public void cancelRandomBooking() {
        List<String> bookedSeats = new ArrayList<>();
        for (Map.Entry<String, SeatStatus> entry : seatStatus.entrySet()) {
            if (entry.getValue() == SeatStatus.BOOKED) {
                bookedSeats.add(entry.getKey());
            }
        }
        
        if (!bookedSeats.isEmpty()) {
            Random rand = new Random();
            String seatId = bookedSeats.get(rand.nextInt(bookedSeats.size()));
            cancelBooking(seatId);
        }
    }
    
    public int getQueueSize() {
        return bookingQueue.size();
    }
    
    public int getTotalBookings() {
        return totalBookings.get();
    }
    
    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }
    
    public int getFailedBookings() {
        return failedBookings.get();
    }
    
    public int getRetryCount() {
        return retryCount.get();
    }
    
    public int getCancellationCount() {
        return cancellationCount.get();
    }
    
    public Set<String> getAllSeats() {
        return seatStatus.keySet();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}