package shortlink.service.impl;


import shortlink.dto.CreateLinkRequest;
import shortlink.exception.DuplicateResourceException;
import shortlink.model.Link;
import shortlink.repository.LinkRepository;
import shortlink.repository.impl.LinkRepositoryImpl;
import shortlink.service.LinkService;
import shortlink.util.validator.ShortCodeValidator;
import shortlink.util.validator.UrlValidator;

/*
* Link service implementation
* */
public class LinkServiceImpl implements LinkService {
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
    * Create new shortlink
    * */
    @Override
    public Link create(CreateLinkRequest req) {
        /*
        * Validate shortcode and original url
        * */
        ShortCodeValidator.validate(req.getShort_code());
        UrlValidator.validate(req.getOriginal_url());


        Link link = new Link();
        link.setShort_code(req.getShort_code().toLowerCase());
        link.setOriginal_url(req.getOriginal_url());

        String shortCodeValidate = findByShortCode(link.getShort_code());

        if(shortCodeValidate != null) {
            throw new DuplicateResourceException("short code already exist");
        }

        Long id = repository.save(link);

        return repository.findById(id);
    }


    /*
    * Find original url by short code
    * */
    @Override
    public String findByShortCode(String shortCode) {

        return repository.findByShortCode(shortCode);
    }

}
