package Question5;

public class BookingRequest {
    private final String userId;
    private final String seatId;
    private final long timestamp;
    
    public BookingRequest(String userId, String seatId) {
        this.userId = userId;
        this.seatId = seatId;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getSeatId() {
        return seatId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return userId + " -> " + seatId;
    }
}