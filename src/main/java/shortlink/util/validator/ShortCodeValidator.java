package shortlink.util.validator;

/*
* Short code validator
* */
public class ShortCodeValidator {
    /*
    * REGEX rules
    *
    * First character (a-z and 0-9)
    * Middle character (a-z, 0-9, "-" and "_")
    * Last character (a-z and 0-9)
    * max length (4-20)
    * short code not empty
    * */
    private static final String REGEX = "^[a-z0-9][a-z0-9_-]{2,18}[a-z0-9]$";

    public static boolean validate(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("short code cannot be empty!");
        }

        if (!code.matches(REGEX)) {
            throw new IllegalArgumentException("Short code should be 4-20 character, " +
                    "only contain \"letter\", \"number\", \"-\", \"_\" " +
                    "and first/last character should be number or letter");
        }

        return true;
    }
}
