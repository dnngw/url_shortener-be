package shortlink.util;

import java.sql.SQLException;

/*
* SQL error helper
* */
public final class SqlErrors {

    /*
    * PostgreSQL error code for a UNIQUE constraint violation
    * */
    private static final String UNIQUE_VIOLATION = "23505";

    private SqlErrors() {}

    /*
    * True when the insert / update failed because the value already exists.
    *
    * Checking "exists" before inserting is not enough: two requests at the same time can both pass
    * the check, then the database UNIQUE constraint rejects the second one.
    * */
    public static boolean isUniqueViolation(SQLException e) {
        return UNIQUE_VIOLATION.equals(e.getSQLState());
    }
}
