package shortlink.util;

/*
* Pagination helper, shared by every paginated list
* */
public final class Pagination {

    /*
    * Largest page size a client can ask for
    * */
    public static final int MAX_PAGE_SIZE = 100;

    private Pagination() {}

    /*
    * Validate page and size, then return the SQL offset
    *
    * Offset is calculated as long, a very big page would overflow int and become negative
    *
    * @Throw IllegalArgumentException when page / size is invalid (→ 400)
    * */
    public static int offset(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page must be at least 1");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Size must be between 1 and " + MAX_PAGE_SIZE);
        }

        long offset = (long) (page - 1) * size;

        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page is too large");
        }

        return (int) offset;
    }
}
