package shortlink.repository;

import shortlink.model.Link;

/*
* Link repository interface
* */
public interface LinkRepository {

    /*
    * Find by id
    * */
    Link findById(Long id);

    /*
    * Find by short code
    * */
    String findByShortCode(String shortCode);

    /*
    * Save data
    * */
    Long save(Link link);

}
