package shortlink.service.impl;


import shortlink.dto.CreateLinkRequest;
import shortlink.dto.PageResponse;
import shortlink.dto.UpdateLinkRequest;
import shortlink.exception.DuplicateResourceException;
import shortlink.exception.NotFoundException;
import shortlink.model.Link;
import shortlink.repository.LinkRepository;
import shortlink.util.Pagination;
import shortlink.service.LinkService;
import shortlink.util.validator.ShortCodeValidator;
import shortlink.util.validator.UrlValidator;

import java.util.List;

/*
* Link service implementation
* */
public class LinkServiceImpl implements LinkService {
    /*
    * Search rules (page / size rules are in Pagination)
    * */
    private static final int MAX_KEYWORD_LENGTH = 100;

    /*
    * Repository
    * */
    private final LinkRepository repository;


    /*
    * Constructor
    * */
    public LinkServiceImpl(LinkRepository repository) {
        this.repository = repository;
    }


    /*
    * Find links by user with pagination
    * */
    @Override
    public PageResponse<Link> getAllByUserId(Long userId, int page, int size, String keyword) {
        String search = keyword == null ? "" : keyword.trim();

        if (search.length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("Search keyword must be at most " + MAX_KEYWORD_LENGTH + " characters");
        }

        int offset = Pagination.offset(page, size);

        List<Link> items = repository.findAllByUserId(userId, search, size, offset);
        int totalItems = repository.countByUserId(userId, search);

        return  new PageResponse<>(items, page, size, totalItems);



    }

    /*
    * Get link by id and user id
    * */
    @Override
    public Link getByIdAndUserId(Long id, Long userId) {
        Link link = repository.findByIdAndUserId(id, userId);

        if (link == null) {
            throw new NotFoundException("Link not found");
        }

        return link;
    }

    /*
    * Create new shortlink
    * */
    @Override
    public Link create(CreateLinkRequest req, Long userId) {
        /*
        * Validate shortcode and original url
        * */
        ShortCodeValidator.validate(req.getShortCode());
        UrlValidator.validate(req.getOriginalUrl());


        Link link = new Link();
        link.setUserId(userId);
        link.setShortCode(req.getShortCode().toLowerCase());
        link.setOriginalUrl(req.getOriginalUrl());

        String shortCodeValidate = getByShortCode(link.getShortCode());

        if(shortCodeValidate != null) {
            throw new DuplicateResourceException("short code already exist");
        }

        Long id = repository.save(link);

        return repository.findById(id);
    }


    /*
    * Get original url by short code
    * */
    @Override
    public String getByShortCode(String shortCode) {

        return repository.findByShortCode(shortCode);
    }

    /*
    * Get link for redirect
    * */
    @Override
    public Link getForRedirect(String shortCode) {
        return repository.findLinkByShortCode(shortCode);
    }

    /*
    * Update original url
    * */
    @Override
    public void updateOriginalUrl(Long id, Long userId, UpdateLinkRequest request) {
        UrlValidator.validate(request.getOriginalUrl());
        boolean updated = repository.updateOriginalUrl(id, userId, request.getOriginalUrl());

        if(!updated) {
            throw new NotFoundException("link not found!");
        }
    }

    /*
    * Delete
    * */
    @Override
    public void delete(Long id, Long userId) {
        boolean deleted = repository.deleteByIdAndUserId(id, userId);

        if(!deleted) {
            throw new NotFoundException("Link not found");
        }
    }

}
