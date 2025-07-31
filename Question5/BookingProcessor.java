package Question5;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class BookingProcessor {
    private final SeatManager seatManager;
    private final BlockingQueue<BookingRequest> bookingQueue;
    private final boolean useOptimistic;
    private final ExecutorService executor;
    private final Consumer<String> logger;

    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);

    // For pause/resume
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public BookingProcessor(SeatManager seatManager, BlockingQueue<BookingRequest> bookingQueue,
                            boolean useOptimistic, int threadCount, Consumer<String> logger) {
        this.seatManager = seatManager;
        this.bookingQueue = bookingQueue;
        this.useOptimistic = useOptimistic;
        this.logger = logger;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public void processBookings() {
        for (int i = 0; i < ((ThreadPoolExecutor) executor).getCorePoolSize(); i++) {
            executor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    // Pause logic
                    synchronized (pauseLock) {
                        while (paused) {
                            try {
                                pauseLock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }

                    try {
                        BookingRequest request = bookingQueue.take();
                        boolean success;
                        if (useOptimistic) {
                            success = seatManager.bookSeatOptimistic(request.seatId, request.userId);
                        } else {
                            success = seatManager.bookSeatPessimistic(request.seatId, request.userId);
                        }
                        if (success) {
                            successCount.incrementAndGet();
                        } else {
                            failCount.incrementAndGet();
                        }
                        logger.accept("User " + request.userId + " booking seat " + request.seatId + (success ? " succeeded" : " failed"));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    public void pause() {
        paused = true;
        logger.accept("Booking processing paused.");
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
        logger.accept("Booking processing resumed.");
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailCount() {
        return failCount.get();
    }
}