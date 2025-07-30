package Question5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingProcessor {
    private final SeatManager seatManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean optimisticMode = true;
    private boolean processing = false;
    
    public BookingProcessor(SeatManager seatManager) {
        this.seatManager = seatManager;
    }
    
    public void setOptimisticMode(boolean optimisticMode) {
        this.optimisticMode = optimisticMode;
    }
    
    public boolean isOptimisticMode() {
        return optimisticMode;
    }
    
    public void startProcessing() {
        if (processing) return;
        
        processing = true;
        executor.submit(() -> {
            while (processing) {
                seatManager.processBookings(optimisticMode);
                try {
                    Thread.sleep(100); // Check for new requests periodically
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
    
    public void stopProcessing() {
        processing = false;
    }
    
    public boolean isProcessing() {
        return processing;
    }
    
    public void shutdown() {
        stopProcessing();
        executor.shutdown();
    }
}