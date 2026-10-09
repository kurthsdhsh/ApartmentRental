package util;

import model.User;

/** Who is logged in. */
public class Session {
    private static User currentUser;

    public static void login(User u) { currentUser = u; }
    public static void logout() { currentUser = null; }
    public static User getCurrentUser() { return currentUser; }
    public static boolean isAdmin() {
        return currentUser != null && Constants.ROLE_ADMIN.equals(currentUser.getRole());
    }
}
