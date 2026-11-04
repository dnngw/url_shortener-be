package shortlink.util.security;

import java.util.concurrent.ConcurrentHashMap;

/*
* Rate Limiter
*
* Fixed window counter per key (ex: ip address). Allows "maxAttempts" hits per "windowMillis",
* the counter starts again when the window is over.
*
* Kept in memory : counters reset when the server restarts, and every server instance
* has its own counters. Enough for a single instance app.
* */
public class RateLimiter {

    /*
    * Counter of one key
    * */
    private record Window(long startMillis, int count) {}

    private final int maxAttempts;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /*
    * Constructor
    * */
    public RateLimiter(int maxAttempts, long windowMillis) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowMillis;
    }

    /*
    * Count one attempt
    *
    * @Return 0 when allowed, otherwise seconds until the key can try again
    * */
    public long hit(String key) {
        long now = System.currentTimeMillis();

        // compute() is atomic per key, so parallel requests are counted correctly
        Window window = windows.compute(key, (k, current) ->
                current == null || isExpired(current, now)
                        ? new Window(now, 1)
                        : new Window(current.startMillis(), current.count() + 1));

        return window.count() > maxAttempts ? secondsLeft(window, now) : 0;
    }

    /*
    * Check without counting
    *
    * @Return 0 when allowed, otherwise seconds until the key can try again
    * */
    public long blockedFor(String key) {
        long now = System.currentTimeMillis();
        Window window = windows.get(key);

        if (window == null || isExpired(window, now) || window.count() < maxAttempts) {
            return 0;
        }

        return secondsLeft(window, now);
    }

    /*
    * Forget the key (ex: login succeeded)
    * */
    public void reset(String key) {
        windows.remove(key);
    }

    /*
    * Remove finished windows, so the map does not grow forever
    * */
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));
    }

    private boolean isExpired(Window window, long now) {
        return now - window.startMillis() >= windowMillis;
    }

    private long secondsLeft(Window window, long now) {
        long millisLeft = window.startMillis() + windowMillis - now;
        // Round up, so "0 seconds left" is never returned while still blocked
        return Math.max(1, (millisLeft + 999) / 1000);
    }
}
