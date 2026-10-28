package com.practice.blog.post;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Plain unit tests: no Spring context, so they run in milliseconds. */
class PostServiceTest {

    @Test
    void excerpt_usesProvidedExcerptWhenPresent() {
        assertThat(PostService.buildExcerpt("Mine", "content")).isEqualTo("Mine");
    }

    @Test
    void excerpt_contentShorterThan150_isWholeContent() {
        assertThat(PostService.buildExcerpt(null, "short")).isEqualTo("short");
    }

    @Test
    void excerpt_contentExactly150_isWholeContent() {
        String content = "x".repeat(150);
        assertThat(PostService.buildExcerpt(null, content)).isEqualTo(content);
    }

    @Test
    void excerpt_contentLongerThan150_isTruncatedTo150() {
        String content = "y".repeat(151);
        assertThat(PostService.buildExcerpt(null, content)).hasSize(150);
    }

    @Test
    void excerpt_blankExcerpt_fallsBackToContent() {
        assertThat(PostService.buildExcerpt("   ", "content")).isEqualTo("content");
        assertThat(PostService.buildExcerpt("", "content")).isEqualTo("content");
    }
}
