package shortlink.exception;

/*
* Forbidden Exception
*
* Request is understood but not allowed (ex: account waiting for approval)
* */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
