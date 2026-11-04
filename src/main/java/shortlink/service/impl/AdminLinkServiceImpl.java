package shortlink.service.impl;

import shortlink.dto.AdminLinkResponse;
import shortlink.dto.PageResponse;
import shortlink.exception.NotFoundException;
import shortlink.model.Link;
import shortlink.model.LinkWithOwner;
import shortlink.repository.LinkRepository;
import shortlink.util.Pagination;
import shortlink.service.AdminLinkService;

import java.util.List;

/*
* Admin link service implementation
* */
public class AdminLinkServiceImpl implements AdminLinkService {

    /*
    * Search rules, same as the other lists (page / size rules are in Pagination)
    * */
    private static final int MAX_KEYWORD_LENGTH = 100;

    /*
    * Repository
    * */
    private final LinkRepository linkRepository;

    /*
    * Constructor
    * */
    public AdminLinkServiceImpl(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    /*
    * Get links
    * */
    @Override
    public PageResponse<AdminLinkResponse> getLinks(String status, Long ownerId, int page, int size, String keyword) {
        String search = keyword == null ? "" : keyword.trim();

        if (search.length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("Search keyword must be at most " + MAX_KEYWORD_LENGTH + " characters");
        }

        Boolean disabled = toDisabledFilter(status);
        int offset = Pagination.offset(page, size);

        List<AdminLinkResponse> items = linkRepository
                .findAllWithOwner(disabled, ownerId, search, size, offset)
                .stream()
                .map(this::toResponse)
                .toList();

        int totalItems = linkRepository.countAllWithOwner(disabled, ownerId, search);

        return new PageResponse<>(items, page, size, totalItems);
    }

    /*
    * Disable
    * */
    @Override
    public void disable(Long linkId) {
        changeDisabled(linkId, false, true);
    }

    /*
    * Enable
    * */
    @Override
    public void enable(Long linkId) {
        changeDisabled(linkId, true, false);
    }

    /*
    * Change the disabled flag from the expected value to the new one
    *
    * @Throw NotFoundException     link not found
    * @Throw IllegalStateException link already has the new value (ex: disable a disabled link)
    * */
    private void changeDisabled(Long linkId, boolean fromDisabled, boolean toDisabled) {
        Link link = linkRepository.findById(linkId);

        if (link == null) {
            throw new NotFoundException("Link not found");
        }

        if (link.isDisabled() != fromDisabled) {
            throw new IllegalStateException(toDisabled ? "Link is already disabled" : "Link is already active");
        }

        // Checked again in the query, in case another admin changed it in the meantime
        if (!linkRepository.updateDisabled(linkId, fromDisabled, toDisabled)) {
            throw new IllegalStateException("Link status has changed, please refresh the page");
        }
    }

    /*
    * Convert status filter to disabled flag
    *
    * all / empty → null (every link), active → false, disabled → true
    * */
    private Boolean toDisabledFilter(String status) {
        if (status == null || status.isBlank() || status.equals("all")) {
            return null;
        }

        return switch (status) {
            case "active" -> false;
            case "disabled" -> true;
            default -> throw new IllegalArgumentException("Unknown link status : " + status);
        };
    }

    /*
    * Map link to response
    * */
    private AdminLinkResponse toResponse(LinkWithOwner row) {
        Link link = row.link();

        return new AdminLinkResponse(
                link.getId(),
                link.getShortCode(),
                link.getOriginalUrl(),
                link.getUserId(),
                row.ownerUsername(),
                link.isDisabled(),
                link.getCreatedAt()
        );
    }
}
