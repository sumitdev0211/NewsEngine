package com.newsshorts.scheduler;

import com.newsshorts.service.NewsFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the news fetch job on a cron schedule (default: every 3 hours).
 */
@Component
public class NewsScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsScheduler.class);

    private final NewsFetchService newsFetchService;

    public NewsScheduler(NewsFetchService newsFetchService) {
        this.newsFetchService = newsFetchService;
    }

    /** Triggers a full fetch for all categories. Interval is configured via fetch.cron. */
    @Scheduled(cron = "${fetch.cron}")
    public void scheduledFetch() {
        log.info("component=fetch message=scheduled_fetch_started");
        try {
            var result = newsFetchService.fetchAll();
            log.info(
                    "component=fetch message=scheduled_fetch_completed categoriesProcessed={} totalFetched={} totalInserted={} totalSkipped={}",
                    result.categoriesProcessed(),
                    result.totalFetched(),
                    result.totalInserted(),
                    result.totalSkipped()
            );
        } catch (Exception ex) {
            log.error("component=fetch message=scheduled_fetch_failed", ex);
        }
    }
}
