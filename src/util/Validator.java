package util;

import java.math.BigDecimal;

public class Validator {
    public static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    public static boolean isEmail(String s) { return s.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"); }
    public static boolean isPhone(String s) { return s.matches("^[0-9+\\-\\s]{7,15}$"); }

    /** Returns the amount or throws NumberFormatException if not a number or not greater than zero. */
    public static BigDecimal parsePositive(String s) {
        BigDecimal v = new BigDecimal(s.trim());
        if (v.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException("must be > 0");
        return v;
    }

    /** Added: amount 0 or more (repair cost, deposit). Throws NumberFormatException otherwise. */
    public static BigDecimal parseNonNegative(String s) {
        BigDecimal v = new BigDecimal(s.trim());
        if (v.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException("must be >= 0");
        return v;
    }

    /** yyyy-MM-dd, throws DateTimeParseException when invalid. */
    public static java.time.LocalDate parseDate(String s) { return java.time.LocalDate.parse(s.trim()); }
}
