package util;

/** ALWAYS use these instead of typing status strings. */
public class Constants {
    public static final String UNIT_AVAILABLE = "Available", UNIT_OCCUPIED = "Occupied",
            UNIT_MAINTENANCE = "Maintenance";
    public static final String CONTRACT_ACTIVE = "Active", CONTRACT_ENDED = "Ended",
            CONTRACT_TERMINATED = "Terminated";
    public static final String PAY_PAID = "Paid", PAY_PARTIAL = "Partial", PAY_VOIDED = "Voided";
    public static final String ROLE_ADMIN = "Admin", ROLE_STAFF = "Staff";
    // Added for the Maintenance module (new constants only, nothing renamed)
    public static final String MAINT_OPEN = "Open", MAINT_IN_PROGRESS = "In Progress",
            MAINT_RESOLVED = "Resolved", MAINT_CANCELLED = "Cancelled";
}
