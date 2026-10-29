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
public enum RoleType {
    ADMIN("admin"),
    USER("user");

    private final String dbName;

    RoleType(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }


    /*
    * Database role value(String) to enum
    * */
    public static RoleType fromDbName(String value) {
        for(RoleType role : values()) {
            if (role.dbName.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("unknown role : " + value);
    }

}
