package com.newsshorts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewsArticleSanitizerTest {

    private NewsArticleSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new NewsArticleSanitizer();
    }

    @Test
    void sanitize_decodesHtmlEntitiesInDescription() {
        var result = sanitizer.sanitize(
                "Headline",
                "Dia Mirza&#039;s statement on climate",
                "<p>Longer body</p>"
        );

        assertThat(result.description()).isEqualTo("Dia Mirza's statement on climate");
    }

    @Test
    void sanitize_stripsHtmlFromContent() {
        var result = sanitizer.sanitize(
                "Headline",
                "Short summary",
                "<ul><li>News</li><li>Education News</li></ul>Actual article text here."
        );

        assertThat(result.content()).isEqualTo("News Education News Actual article text here.");
    }

    @Test
    void sanitize_storesNullContentWhenIdenticalToDescription() {
        var result = sanitizer.sanitize(
                "Google News",
                "Comprehensive up-to-date news coverage.",
                "Comprehensive up-to-date news coverage."
        );

        assertThat(result.content()).isNull();
        assertThat(result.description()).isEqualTo("Comprehensive up-to-date news coverage.");
    }

    @Test
    void sanitize_storesNullContentWhenBlank() {
        var result = sanitizer.sanitize("Title", "Summary", "   ");

        assertThat(result.content()).isNull();
    }

    @Test
    void sanitize_keepsContentThatStartsWithDescriptionButAddsMoreText() {
        var description = "A hero is incomplete without a villain.";
        var content = description + " If movies and series teach us anything… [+1200 chars]";

        var result = sanitizer.sanitize("Title", description, content);

        assertThat(result.content()).isEqualTo(content);
    }

    @Test
    void sanitize_keepsDistinctContent() {
        var result = sanitizer.sanitize(
                "Title",
                "Short summary for the card.",
                "A much longer unique body that adds new information beyond the summary."
        );

        assertThat(result.content()).isEqualTo(
                "A much longer unique body that adds new information beyond the summary."
        );
    }
}
