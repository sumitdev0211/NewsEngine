package com.newsshorts.service;

import com.newsshorts.dto.api.FetchNowResponse;
import com.newsshorts.dto.newsapi.NewsApiArticle;
import com.newsshorts.entity.NewsArticle;
import com.newsshorts.enums.Category;
import com.newsshorts.integration.NewsApiClient;
import com.newsshorts.repository.NewsArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Write-side service — pulls articles from NewsAPI, deduplicates by source URL, and saves new rows.
 */
@Service
public class NewsFetchService {

    private static final Logger log = LoggerFactory.getLogger(NewsFetchService.class);

    private final NewsApiClient newsApiClient;
    private final NewsArticleRepository repository;
    private final NewsArticleSanitizer sanitizer;

    public NewsFetchService(
            NewsApiClient newsApiClient,
            NewsArticleRepository repository,
            NewsArticleSanitizer sanitizer
    ) {
        this.newsApiClient = newsApiClient;
        this.repository = repository;
        this.sanitizer = sanitizer;
    }

    /**
     * Fetches headlines for every configured category and aggregates the results.
     * Called by both the scheduler and the admin fetch-now endpoint.
     */
    @Transactional
    public FetchNowResponse fetchAll() {
        int categoriesProcessed = 0;
        int totalFetched = 0;
        int totalInserted = 0;
        int totalSkipped = 0;

        for (Category category : Category.values()) {
            CategoryFetchResult result = fetchCategory(category);
            categoriesProcessed++;
            totalFetched += result.fetched();
            totalInserted += result.inserted();
            totalSkipped += result.skipped();
        }

        return new FetchNowResponse(categoriesProcessed, totalFetched, totalInserted, totalSkipped);
    }

    /** Fetches and persists articles for one category, skipping duplicates. */
    private CategoryFetchResult fetchCategory(Category category) {
        long startMs = System.currentTimeMillis();
        log.info("component=fetch category={} message=fetch_started", category);

        List<NewsApiArticle> articles = newsApiClient.fetchTopHeadlines(category);
        int inserted = 0;
        int skipped = 0;

        for (NewsApiArticle apiArticle : articles) {
            if (!StringUtils.hasText(apiArticle.url())) {
                skipped++;
                continue;
            }

            if (repository.existsBySourceUrl(apiArticle.url())) {
                skipped++;
                continue;
            }

            NewsArticle entity = mapToEntity(apiArticle, category);
            try {
                repository.save(entity);
                inserted++;
            } catch (DataIntegrityViolationException ex) {
                skipped++;
                log.debug(
                        "component=fetch category={} message=duplicate_skipped sourceUrl={}",
                        category,
                        apiArticle.url()
                );
            }
        }

        long durationMs = System.currentTimeMillis() - startMs;
        log.info(
                "component=fetch category={} fetched={} inserted={} skipped={} durationMs={}",
                category,
                articles.size(),
                inserted,
                skipped,
                durationMs
        );

        return new CategoryFetchResult(articles.size(), inserted, skipped);
    }

    private NewsArticle mapToEntity(NewsApiArticle apiArticle, Category category) {
        NewsArticleSanitizer.SanitizedArticleText cleanText = sanitizer.sanitize(
                apiArticle.title(),
                apiArticle.description(),
                apiArticle.content()
        );

        NewsArticle article = new NewsArticle();
        article.setTitle(cleanText.title());
        article.setDescription(cleanText.description());
        article.setContent(cleanText.content());
        article.setImageUrl(apiArticle.urlToImage());
        article.setSourceUrl(apiArticle.url());
        article.setCategory(category);
        article.setPublishedAt(parsePublishedAt(apiArticle.publishedAt()));

        if (apiArticle.source() != null) {
            article.setSourceName(apiArticle.source().name());
        }

        return article;
    }

    private Instant parsePublishedAt(String publishedAt) {
        if (!StringUtils.hasText(publishedAt)) {
            return null;
        }
        try {
            return Instant.parse(publishedAt);
        } catch (DateTimeParseException ex) {
            log.warn("component=fetch message=invalid_published_at value={}", publishedAt);
            return null;
        }
    }

    private record CategoryFetchResult(int fetched, int inserted, int skipped) {
    }
}
