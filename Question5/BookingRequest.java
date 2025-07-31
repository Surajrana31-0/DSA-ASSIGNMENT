package Question5;

public class BookingRequest {
    public final String userId;
    public final String seatId;

    public BookingRequest(String userId, String seatId) {
        this.userId = userId;
        this.seatId = seatId;
    }
}