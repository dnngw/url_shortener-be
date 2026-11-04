package shortlink.exception;

/*
* Too Many Requests Exception
*
* Client hit a rate limit (→ 429). retryAfterSeconds goes to the Retry-After header
* */
public class TooManyRequestsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyRequestsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
