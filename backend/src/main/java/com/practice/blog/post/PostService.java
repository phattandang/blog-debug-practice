package com.practice.blog.post;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practice.blog.common.ForbiddenException;
import com.practice.blog.common.NotFoundException;

@Service
public class PostService {

    static final int EXCERPT_LENGTH = 150;

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PostResponse create(PostRequest request, String userId) {
        Post post = new Post();
        applyRequest(post, request);
        post.setAuthorId(userId);
        post.setPublished(true);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(post.getCreatedAt());
        return PostResponse.from(repository.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> list(String category) {
        List<Post> posts = (category == null || category.isBlank())
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category);
        return posts.stream().map(PostResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        return PostResponse.from(findOrThrow(id));
    }

    @Transactional
    public PostResponse update(Long id, PostRequest request, String userId) {
        Post post = findOrThrow(id);
        checkOwner(post, userId);
        applyRequest(post, request);
        post.setUpdatedAt(Instant.now());
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long id, String userId) {
        Post post = findOrThrow(id);
        checkOwner(post, userId);
        repository.delete(post);
    }

    static String buildExcerpt(String excerpt, String content) {
        if (excerpt != null && !excerpt.isBlank()) {
            return excerpt;
        }
        return content.length() <= EXCERPT_LENGTH ? content : content.substring(0, EXCERPT_LENGTH);
    }

    private void applyRequest(Post post, PostRequest request) {
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setCategory(request.category());
        post.setExcerpt(buildExcerpt(request.excerpt(), request.content()));
        post.setTags(request.tags() == null ? new ArrayList<>() : new ArrayList<>(request.tags()));
        post.setReadTime(request.readTime());
    }

    private Post findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post " + id + " not found"));
    }

    private void checkOwner(Post post, String userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You can only modify your own posts");
        }
    }
}
