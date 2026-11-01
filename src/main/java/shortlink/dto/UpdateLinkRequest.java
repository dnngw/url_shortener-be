package shortlink.dto;

public class UpdateLinkRequest {
    /*
     * Original URL
     * */
    private String originalUrl;

    /*
    * Constructor
    * */
    public UpdateLinkRequest(){}

    /*
    * Get original url
    * */
    public String getOriginalUrl() { return originalUrl; }

    /*
    * Set original url
    * */
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
}
