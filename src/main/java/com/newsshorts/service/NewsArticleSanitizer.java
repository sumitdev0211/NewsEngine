package com.newsshorts.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central place for all news text cleanup rules applied before articles are saved to the database.
 * <p>
 * Add new sanitization rules here so they stay in one trackable location rather than
 * scattered across fetch or API code.
 */
@Component
public class NewsArticleSanitizer {

    /** Matches HTML tags such as {@code <p>}, {@code <ul>}, {@code <li>}. */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    /** Matches decimal numeric entities, e.g. {@code &#039;}. */
    private static final Pattern DECIMAL_ENTITY_PATTERN = Pattern.compile("&#(\\d+);");

    /** Matches hexadecimal numeric entities, e.g. {@code &#x27;}. */
    private static final Pattern HEX_ENTITY_PATTERN = Pattern.compile("&#x([0-9a-fA-F]+);");

    /**
     * Applies all active sanitization rules to raw NewsAPI text fields.
     *
     * @param title       raw title from NewsAPI (may be null)
     * @param description raw description from NewsAPI (may be null)
     * @param content     raw content snippet from NewsAPI (may be null)
     * @return cleaned values ready to persist on {@link com.newsshorts.entity.NewsArticle}
     */
    public SanitizedArticleText sanitize(String title, String description, String content) {
        String cleanTitle = sanitizeTitle(title);
        String cleanDescription = sanitizeDescription(description);
        String cleanContent = sanitizeContent(content, cleanDescription);

        return new SanitizedArticleText(cleanTitle, cleanDescription, cleanContent);
    }

    /**
     * Rule: decode HTML entities and trim whitespace on titles.
     */
    private String sanitizeTitle(String title) {
        return normalizeWhitespace(decodeHtmlEntities(title));
    }

    /**
     * Rule: decode HTML entities and trim whitespace on descriptions.
     * Descriptions are shown in the feed card — keep them readable but do not strip tags here
     * unless they appear (NewsAPI usually sends plain text for description).
     */
    private String sanitizeDescription(String description) {
        return normalizeWhitespace(decodeHtmlEntities(description));
    }

    /**
     * Rule set for the longer {@code content} field:
     * <ol>
     *   <li>Decode HTML entities (e.g. {@code &#039;} → {@code '})</li>
     *   <li>Strip HTML tags (publishers sometimes embed {@code <ul>}, {@code <li>}, etc.)</li>
     *   <li>Normalize whitespace after cleanup</li>
     *   <li>If content is blank or identical to description → store {@code null}
     *       so detail views can fall back to description instead of showing duplicate text</li>
     * </ol>
     */
    private String sanitizeContent(String rawContent, String cleanDescription) {
        String cleaned = normalizeWhitespace(stripHtmlTags(decodeHtmlEntities(rawContent)));

        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        if (!StringUtils.hasText(cleanDescription)) {
            return cleaned;
        }

        if (cleaned.equals(cleanDescription)) {
            return null;
        }

        return cleaned;
    }

    /**
     * Removes HTML tags from text. Replaces each tag with a space so words from adjacent
     * elements stay separated — e.g. {@code <li>News</li><li>More</li>} → {@code News More}.
     */
    String stripHtmlTags(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        return HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
    }

    /**
     * Decodes common HTML entities returned by NewsAPI and publisher feeds.
     * Handles named entities ({@code &amp;}) and numeric forms ({@code &#039;}, {@code &#x27;}).
     */
    String decodeHtmlEntities(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }

        String decoded = text;
        decoded = decoded.replace("&nbsp;", " ");
        decoded = decoded.replace("&amp;", "&");
        decoded = decoded.replace("&lt;", "<");
        decoded = decoded.replace("&gt;", ">");
        decoded = decoded.replace("&quot;", "\"");
        decoded = decoded.replace("&apos;", "'");
        decoded = decoded.replace("&#39;", "'");
        decoded = decoded.replace("&#039;", "'");

        decoded = decodeNumericEntities(decoded, DECIMAL_ENTITY_PATTERN, 10);
        decoded = decodeNumericEntities(decoded, HEX_ENTITY_PATTERN, 16);

        return decoded;
    }

    private String decodeNumericEntities(String text, Pattern pattern, int radix) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            int codePoint = Integer.parseInt(matcher.group(1), radix);
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf((char) codePoint)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Collapses repeated whitespace and trims leading/trailing space. */
    private String normalizeWhitespace(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Clean text fields produced by {@link #sanitize(String, String, String)}.
     */
    public record SanitizedArticleText(String title, String description, String content) {
    }
}
