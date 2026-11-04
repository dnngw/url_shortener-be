package shortlink.model;

/*
* Link with the username of its owner
*
* Read only result for the admin link list
* */
public record LinkWithOwner(Link link, String ownerUsername) {
}
