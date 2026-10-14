package shortlink.service;

import shortlink.dto.CreateLinkRequest;
import shortlink.model.Link;

/*
* Link service
* */
public interface LinkService {

    /*
    * Create Link
    * */
    public Link create(CreateLinkRequest req);

    /*
    * Find original_url by short code
    * */
    public String findByShortCode(String shortCode);


}
