package com.newsshorts.service;

import com.newsshorts.dto.newsapi.NewsApiArticle;
import com.newsshorts.dto.newsapi.NewsApiSource;
import com.newsshorts.entity.NewsArticle;
import com.newsshorts.enums.Category;
import com.newsshorts.integration.NewsApiClient;
import com.newsshorts.repository.NewsArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class NewsFetchServiceIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("PGHOST", () -> System.getenv("PGHOST"));
        registry.add("PGDATABASE", () -> System.getenv("PGDATABASE"));
        registry.add("PGUSER", () -> System.getenv("PGUSER"));
        registry.add("PGPASSWORD", () -> System.getenv("PGPASSWORD"));
        registry.add("PGSSLMODE", () -> System.getenv("PGSSLMODE"));
        registry.add("NEWSAPI_KEY", () -> System.getenv("NEWSAPI_KEY"));
    }

    @Autowired
    private NewsFetchService newsFetchService;

    @Autowired
    private NewsArticleRepository repository;

    @MockBean
    private NewsApiClient newsApiClient;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void fetchAll_insertsNewArticlesAndSkipsDuplicates() {
        NewsApiArticle article = new NewsApiArticle(
                "Mock Headline",
                "Mock description",
                "Mock content",
                "https://example.com/mock-article",
                "https://example.com/mock-image.jpg",
                "2026-06-21T00:00:00Z",
                new NewsApiSource("test", "Mock Source")
        );

        when(newsApiClient.fetchTopHeadlines(Category.GENERAL)).thenReturn(List.of(article));
        when(newsApiClient.fetchTopHeadlines(Category.ENTERTAINMENT)).thenReturn(List.of());

        var firstRun = newsFetchService.fetchAll();
        assertThat(firstRun.totalInserted()).isEqualTo(1);
        assertThat(firstRun.totalSkipped()).isEqualTo(0);
        assertThat(repository.count()).isEqualTo(1);

        var secondRun = newsFetchService.fetchAll();
        assertThat(secondRun.totalInserted()).isEqualTo(0);
        assertThat(secondRun.totalSkipped()).isEqualTo(1);
        assertThat(repository.count()).isEqualTo(1);

        NewsArticle saved = repository.findAll().get(0);
        assertThat(saved.getTitle()).isEqualTo("Mock Headline");
        assertThat(saved.getCategory()).isEqualTo(Category.GENERAL);
        assertThat(saved.getSourceUrl()).isEqualTo("https://example.com/mock-article");
    }
}
