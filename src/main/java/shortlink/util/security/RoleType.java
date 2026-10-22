package shortlink.util.security;

/*
* Role
*
* {
*   Role value from ENUM to String or String to ENUM
*   this condition to convert value type on the database and system
*
* }
*
* */
public enum Role {
    ADMIN("admin"),
    USER("user");

    private final String dbName;

    Role(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }


    /*
    * Database role value(String) to enum
    * */
    public static Role fromDbName(String value) {
        for(Role role : values()) {
            if (role.dbName.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("unknown role : " + value);
    }

}
