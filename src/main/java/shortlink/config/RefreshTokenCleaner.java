package shortlink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.repository.RefreshTokenRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
* Refresh Token Cleaner
*
* Every login and every refresh adds a row in refresh_tokens, the old row is only marked revoked.
* This job deletes expired and revoked tokens so the table does not grow forever.
*
* Runs once when the app starts, then every 24 hours (inside the app, no OS cron needed)
* */
public class RefreshTokenCleaner {
    /*
    * Logger
    * */
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCleaner.class);

    /*
    * Hours between two cleanups
    * */
    private static final long INTERVAL_HOURS = 24;

    /*
    * Repository
    * */
    private final RefreshTokenRepository refreshTokenRepository;

    /*
    * Constructor
    * */
    public RefreshTokenCleaner(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /*
    * Start the scheduled cleanup
    * */
    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "refresh-token-cleaner");
            // Daemon thread does not keep the app running on shutdown
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(this::cleanup, 0, INTERVAL_HOURS, TimeUnit.HOURS);
    }

    /*
    * Delete dead tokens
    *
    * Errors are only logged : an exception inside a scheduled task would stop every next run
    * */
    private void cleanup() {
        try {
            int deleted = refreshTokenRepository.deleteExpiredAndRevoked();
            logger.info("Refresh token cleanup : {} expired / revoked token(s) deleted", deleted);

        } catch (Exception e) {
            logger.error("Refresh token cleanup failed", e);
        }
    }
}
