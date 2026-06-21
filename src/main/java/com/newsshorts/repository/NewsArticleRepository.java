package com.newsshorts.repository;

import com.newsshorts.entity.NewsArticle;
import com.newsshorts.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for the news_article table. */
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    /** Used during fetch to skip articles already stored (dedup by original article URL). */
    boolean existsBySourceUrl(String sourceUrl);

    /** Feed query: articles in one category, newest publish date first. */
    Page<NewsArticle> findByCategoryOrderByPublishedAtDesc(Category category, Pageable pageable);

    /** Feed query: all categories combined, newest publish date first. */
    Page<NewsArticle> findAllByOrderByPublishedAtDesc(Pageable pageable);
}
