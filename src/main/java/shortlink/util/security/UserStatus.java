package shortlink.util.security;

/*
* User account status
*
* PENDING  : registered, waiting for admin approval
* ACTIVE   : approved, can login and use the app
* INACTIVE : deactivated by admin
* */
public enum UserStatus {
    PENDING("pending"),
    ACTIVE("active"),
    INACTIVE("inactive");

    /*
    * Value stored in database
    * */
    private final String dbName;

    UserStatus(String dbName) {
        this.dbName = dbName;
    }

    /*
    * Get database value
    * */
    public String getDbName() {
        return dbName;
    }

    /*
    * Convert database / request value to enum
    * */
    public static UserStatus fromDbName(String value) {
        for (UserStatus status : values()) {
            if (status.dbName.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status : " + value);
    }
}
