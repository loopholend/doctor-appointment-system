package common;

/**
 * String constants for the appointment_status column.
 * Centralises the literals so they cannot drift out of sync across servlets.
 */
public final class AppointmentStatus {
    private AppointmentStatus() {}

    public static final String BOOKED    = "booked";
    public static final String CANCELLED = "cancelled";
    public static final String COMPLETED = "completed";
}
