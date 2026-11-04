package shortlink.util.security;

import shortlink.exception.TooManyRequestsException;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
* Auth Rate Limit
*
* Login :
*   - failed attempts per ip + username : 5 / 15 minutes (stops brute force on one account)
*   - every attempt per ip              : 20 / 15 minutes (stops one ip trying many usernames)
*   Failures are counted per ip + username, not per username only, so someone else
*   can not lock an account (ex: admin) by typing a wrong password on purpose.
*
* Register :
*   - every attempt per ip : 5 / hour (stops filling the pending list with spam)
* */
public class AuthRateLimit {

    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final Duration REGISTER_WINDOW = Duration.ofHours(1);

    private final RateLimiter loginFailures = new RateLimiter(5, LOGIN_WINDOW.toMillis());
    private final RateLimiter loginAttempts = new RateLimiter(20, LOGIN_WINDOW.toMillis());
    private final RateLimiter registerAttempts = new RateLimiter(5, REGISTER_WINDOW.toMillis());

    /*
    * Constructor
    *
    * Starts a background task that removes old counters every 5 minutes
    * */
    public AuthRateLimit() {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "auth-rate-limit-cleaner");
            // Daemon thread does not keep the app running on shutdown
            thread.setDaemon(true);
            return thread;
        });

        cleaner.scheduleAtFixedRate(() -> {
            loginFailures.purgeExpired();
            loginAttempts.purgeExpired();
            registerAttempts.purgeExpired();
        }, 5, 5, TimeUnit.MINUTES);
    }

    /*
    * Call before checking the password
    *
    * @Throw TooManyRequestsException when the ip or the ip + username is blocked
    * */
    public void beforeLogin(String ip, String username) {
        long ipBlocked = loginAttempts.hit(ip);
        if (ipBlocked > 0) {
            throw tooMany("Too many login attempts", ipBlocked);
        }

        long accountBlocked = loginFailures.blockedFor(failureKey(ip, username));
        if (accountBlocked > 0) {
            throw tooMany("Too many failed login attempts", accountBlocked);
        }
    }

    /*
    * Call when the username / password is wrong
    * */
    public void loginFailed(String ip, String username) {
        loginFailures.hit(failureKey(ip, username));
    }

    /*
    * Call when the login succeeded, previous failures of this ip + username are forgotten
    * */
    public void loginSucceeded(String ip, String username) {
        loginFailures.reset(failureKey(ip, username));
    }

    /*
    * Call before creating the account
    *
    * @Throw TooManyRequestsException when the ip registered too many times
    * */
    public void beforeRegister(String ip) {
        long blocked = registerAttempts.hit(ip);
        if (blocked > 0) {
            throw tooMany("Too many registration attempts", blocked);
        }
    }

    /*
    * Username is normalized like the login query (trim + lowercase), so "Alice" and "alice" share the counter
    * */
    private String failureKey(String ip, String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return ip + "|" + normalized;
    }

    /*
    * Build the exception with a readable wait time, ex: "try again in 12 minutes"
    * */
    private TooManyRequestsException tooMany(String reason, long retryAfterSeconds) {
        long minutes = (retryAfterSeconds + 59) / 60;
        String wait = retryAfterSeconds < 60
                ? retryAfterSeconds + (retryAfterSeconds == 1 ? " second" : " seconds")
                : minutes + (minutes == 1 ? " minute" : " minutes");

        return new TooManyRequestsException(reason + ", try again in " + wait, retryAfterSeconds);
    }
}
