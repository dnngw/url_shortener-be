package shortlink.util;

/*
* Search Pattern
*
* Build pattern for SQL "ILIKE ? ESCAPE '\'"
* */
public final class SearchPattern {

    private SearchPattern() {}

    /*
    * Contains pattern
    *
    * Escape LIKE wildcard (% and _) from user input, so "a_b" is searched literally
    * Empty keyword → "%%" match every row
    * */
    public static String contains(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return "%%";
        }

        String escaped = keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return "%" + escaped + "%";
    }
}
