package shortlink.model;

import java.time.LocalDateTime;

/*
* Link model
* */
public class Link {
    /*
    * Id
    * */
    private Long id;

    /*
    * Short code
    * */
    private String short_code;

    /*
    * Original url
    * */
    private String original_url;

    /*
    * Created at
    * */
    private LocalDateTime created_at;

    /*
    * Default Constructor
    * */
    public Link() {};


    /*
    * Constructor
    * */
    public Link(Long id, String short_code, String original_url, LocalDateTime created_at) {
        this.id = id;
        this.short_code = short_code;
        this.original_url = original_url;
        this.created_at = created_at;
    }


    /*
    * Get id
    *
    * return id
    * */
    public Long getId() {
        return id;
    }

    /*
     * Set id
     *
     * param id
     * */
    public void setId(Long id) {
        this.id = id;
    }

    /*
     * Get short code
     *
     * return short_code
     * */
    public String getShort_code() {
        return short_code;
    }

    /*
     * Set short code
     *
     * param short_code
     * */
    public void setShort_code(String short_code) {
        this.short_code = short_code;
    }

    /*
     * Get original url
     *
     * return original_url
     * */
    public String getOriginal_url() {
        return original_url;
    }


    /*
     * Set original url
     *
     * param original_url
     * */
    public void setOriginal_url(String original_url) {
        this.original_url = original_url;
    }

    /*
     * Get created at
     *
     * return created_at
     * */
    public LocalDateTime getCreated_at() {
        return created_at;
    }


    /*
    * Set created at
    *
    * param created_at
    * */
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
