public class UserSession {
    private static int loggedInUserId = -1;

    // Setters and getters
    public static void setLoggedInUserId(int userId) {
        loggedInUserId = userId;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static boolean isLoggedIn() {
        return loggedInUserId != -1;
    }
}
