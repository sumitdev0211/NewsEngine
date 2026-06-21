package com.newsshorts.integration;

import com.newsshorts.config.NewsApiProperties;
import com.newsshorts.config.NewsApiProperties.CategoryFetchConfig;
import com.newsshorts.dto.newsapi.NewsApiResponse;
import com.newsshorts.enums.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

/**
 * HTTP client for NewsAPI.org.
 * <p>
 * Fetch strategy per category is configured in {@code application.properties}:
 * <ul>
 *   <li>{@code newsapi.category.general.sources} — top headlines from listed source IDs</li>
 *   <li>{@code newsapi.category.entertainment.search-query} — keyword search via /everything</li>
 * </ul>
 */
@Component
public class NewsApiClient {

    private static final Logger log = LoggerFactory.getLogger(NewsApiClient.class);

    private final WebClient webClient;
    private final String apiKey;
    private final NewsApiProperties properties;

    public NewsApiClient(
            WebClient newsApiWebClient,
            @Value("${newsapi.key}") String apiKey,
            NewsApiProperties properties
    ) {
        this.webClient = newsApiWebClient;
        this.apiKey = apiKey;
        this.properties = properties;
    }

    /**
     * Fetches articles for the given app category using the configured strategy (sources or search).
     * Returns an empty list on HTTP errors or malformed responses (does not throw).
     */
    public List<com.newsshorts.dto.newsapi.NewsApiArticle> fetchTopHeadlines(Category category) {
        CategoryFetchConfig config = properties.configFor(category);

        if (StringUtils.hasText(config.getSources())) {
            return fetchFromSources(category, config.getSources().trim());
        }
        if (StringUtils.hasText(config.getSearchQuery())) {
            return fetchFromSearch(category, config.getSearchQuery().trim());
        }

        log.warn(
                "component=fetch category={} message=no_fetch_config " +
                        "hint=Set newsapi.category.{}.sources or .search-query in application.properties",
                category,
                category.name().toLowerCase()
        );
        return Collections.emptyList();
    }

    /**
     * GET /top-headlines?sources=... — curated headlines from specific publishers.
     * Note: cannot combine {@code sources} with {@code country} or {@code category} params.
     */
    private List<com.newsshorts.dto.newsapi.NewsApiArticle> fetchFromSources(Category category, String sources) {
        log.info("component=fetch category={} strategy=sources sources={}", category, sources);

        return executeRequest(
                category,
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/top-headlines")
                                .queryParam("sources", sources)
                                .queryParam("pageSize", properties.getPageSize())
                                .queryParam("apiKey", apiKey)
                                .build())
        );
    }

    /**
     * GET /everything?q=... — keyword search when no dedicated headline sources exist.
     */
    private List<com.newsshorts.dto.newsapi.NewsApiArticle> fetchFromSearch(Category category, String searchQuery) {
        log.info("component=fetch category={} strategy=search query={}", category, searchQuery);

        return executeRequest(
                category,
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/everything")
                                .queryParam("q", searchQuery)
                                .queryParam("language", "en")
                                .queryParam("sortBy", "publishedAt")
                                .queryParam("pageSize", properties.getPageSize())
                                .queryParam("apiKey", apiKey)
                                .build())
        );
    }

    private List<com.newsshorts.dto.newsapi.NewsApiArticle> executeRequest(Category category, WebClient.RequestHeadersSpec<?> request) {
        try {
            NewsApiResponse response = request
                    .retrieve()
                    .bodyToMono(NewsApiResponse.class)
                    .block();

            if (response == null || response.articles() == null) {
                log.warn("component=fetch category={} message=empty_response", category);
                return Collections.emptyList();
            }

            if (!"ok".equalsIgnoreCase(response.status())) {
                log.error("component=fetch category={} message=newsapi_error status={}", category, response.status());
                return Collections.emptyList();
            }

            return response.articles();
        } catch (WebClientResponseException ex) {
            log.error(
                    "component=fetch category={} message=newsapi_http_error status={} body={}",
                    category,
                    ex.getStatusCode().value(),
                    ex.getResponseBodyAsString(),
                    ex
            );
            return Collections.emptyList();
        } catch (Exception ex) {
            log.error("component=fetch category={} message=newsapi_unexpected_error", category, ex);
            return Collections.emptyList();
        }
    }
}
