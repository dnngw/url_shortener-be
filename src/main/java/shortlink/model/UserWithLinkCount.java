package shortlink.model;

/*
* User with the number of links they created
*
* Read only result for the admin user list
* */
public record UserWithLinkCount(User user, int linkCount) {
}
