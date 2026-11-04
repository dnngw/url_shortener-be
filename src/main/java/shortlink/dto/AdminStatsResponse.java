package shortlink.dto;

/*
* Admin Stats Response
*
* Numbers shown on the admin dashboard
* */
public class AdminStatsResponse {
    /*
    * Users waiting for approval
    * */
    private final int pendingUsers;

    /*
    * Approved users
    * */
    private final int activeUsers;

    /*
    * Deactivated users
    * */
    private final int inactiveUsers;

    /*
    * Every link created in the app
    * */
    private final int totalLinks;

    /*
    * Constructor
    * */
    public AdminStatsResponse(int pendingUsers, int activeUsers, int inactiveUsers, int totalLinks) {
        this.pendingUsers = pendingUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.totalLinks = totalLinks;
    }

    public int getPendingUsers() { return pendingUsers; }
    public int getActiveUsers() { return activeUsers; }
    public int getInactiveUsers() { return inactiveUsers; }
    public int getTotalLinks() { return totalLinks; }
}
