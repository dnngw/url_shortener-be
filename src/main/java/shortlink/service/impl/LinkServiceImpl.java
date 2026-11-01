package shortlink.service.impl;


import shortlink.dto.CreateLinkRequest;
import shortlink.dto.PageResponse;
import shortlink.dto.UpdateLinkRequest;
import shortlink.exception.DuplicateResourceException;
import shortlink.exception.NotFoundException;
import shortlink.model.Link;
import shortlink.repository.LinkRepository;
import shortlink.service.LinkService;
import shortlink.util.validator.ShortCodeValidator;
import shortlink.util.validator.UrlValidator;

import java.util.List;

/*
* Link service implementation
* */
public class LinkServiceImpl implements LinkService {
    /*
    * Pagination rules
    * */
    private static final int MAX_PAGE_SIZE = 100;

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
    public PageResponse<Link> getAllByUserId(Long userId, int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page must be at leat 1");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Size must be between 1 and " + MAX_PAGE_SIZE);
        }

        int offset = (page - 1) * size;

        List<Link> items = repository.findAllByUserId(userId, size, offset);
        int totalItems = repository.countByUserId(userId);

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
