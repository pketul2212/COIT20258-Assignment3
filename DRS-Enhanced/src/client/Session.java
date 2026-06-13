package client;

import model.User;

/**
 * Singleton session manager tracking the currently logged-in user.
 */
public class Session {

    private static Session instance;
    private User currentUser;

    private Session() {
    }

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public boolean isLoggedIn() { return currentUser != null; }
    public boolean isAdmin() { return currentUser != null && currentUser.getRole() == User.Role.ADMIN; }

    public boolean canManageTeams() {
        return currentUser != null
                && (currentUser.getRole() == User.Role.ADMIN
                    || currentUser.getRole() == User.Role.COORDINATOR);
    }

    public boolean canManageResources() {
        return canManageTeams();
    }

    public void logout() { currentUser = null; }
}
