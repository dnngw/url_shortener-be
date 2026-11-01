package shortlink.dto;

import java.util.List;
import java.util.Map;

/*
* Page Response
*
* Generic response for paginated data
* */
public class PageResponse<T> {

    /*
    * Items
    * */
    private final List<T> items;

    /*
    * Page
    * */
    private final int page;

    /*
    * Size
    * */
    private final int size;

    /*
    * Total items
    * */

    private final int totalItems;

    /*
    * Total pages
    * */
    private final int totalPages;

    public PageResponse(List<T> items, int page, int size, int totalItems) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages= (int) Math.ceil((double) totalItems / size);
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
}
