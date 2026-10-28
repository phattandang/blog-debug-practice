package com.practice.blog.post;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        String category,
        String excerpt,
        List<String> tags,
        Integer readTime,
        boolean published,
        String authorId,
        Instant createdAt,
        Instant updatedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getExcerpt(),
                List.copyOf(post.getTags()),
                post.getReadTime(),
                post.isPublished(),
                post.getAuthorId(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
