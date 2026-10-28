package com.practice.blog.post;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Full-stack API tests (real service, real H2 database).
 * One test per line of SPEC.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PostApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    PostRepository repository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    // ---------- POST /api/posts ----------

    @Test
    void create_returns201WithFullResponseShape() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hello","content":"Some content","category":"tech",
                                 "excerpt":"Custom excerpt","tags":["java","spring"],"readTime":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Hello")))
                .andExpect(jsonPath("$.content", is("Some content")))
                .andExpect(jsonPath("$.category", is("tech")))
                .andExpect(jsonPath("$.excerpt", is("Custom excerpt")))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[0]", is("java")))
                .andExpect(jsonPath("$.readTime", is(5)))
                .andExpect(jsonPath("$.published", is(true)))
                .andExpect(jsonPath("$.authorId", is("user-1")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void create_onlyRequiredFields_succeeds() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hello","content":"Short","category":"tech"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.excerpt", is("Short")))
                .andExpect(jsonPath("$.tags", hasSize(0)))
                .andExpect(jsonPath("$.readTime").doesNotExist())
                .andExpect(jsonPath("$.published", is(true)));
    }

    @Test
    void create_publishedIsAlwaysTrue_evenIfClientSendsFalse() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hello","content":"Body","category":"tech","published":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.published", is(true)));
    }

    @Test
    void create_autoExcerptIsFirst150CharsOfContent() throws Exception {
        String content = "a".repeat(150) + "THIS_SHOULD_NOT_APPEAR";
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(content, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.excerpt", is("a".repeat(150))));
    }

    @Test
    void create_blankExcerptIsTreatedAsMissing() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Body text", "   ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.excerpt", is("Body text")));
    }

    @Test
    void create_nullTagsBecomesEmptyList() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","content":"C","category":"tech","tags":null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags", hasSize(0)));
    }

    @Test
    void create_missingUserHeader_returns400() throws Exception {
        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","content":"C","category":"tech"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing required header: x-user-id")));
    }

    @Test
    void create_blankRequiredFields_returns400WithFieldDetails() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"  ","content":"","category":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation failed")))
                .andExpect(jsonPath("$.details.title", is("title is required")))
                .andExpect(jsonPath("$.details.content", is("content is required")))
                .andExpect(jsonPath("$.details.category", is("category is required")));
    }

    @Test
    void create_readTimeAsNonNumericString_returns400() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","content":"C","category":"tech","readTime":"five"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Malformed request body")));
    }

    // ---------- GET /api/posts ----------

    @Test
    void list_returnsAllPosts() throws Exception {
        createPost("user-1", "tech");
        createPost("user-1", "life");

        mvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tags", notNullValue()));
    }

    @Test
    void list_filtersByCategory_caseInsensitive() throws Exception {
        createPost("user-1", "tech");
        createPost("user-1", "life");
        createPost("user-2", "Tech");

        mvc.perform(get("/api/posts").param("category", "TECH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---------- GET /api/posts/{id} ----------

    @Test
    void get_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/posts/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Post 99999 not found")));
    }

    // ---------- PUT /api/posts/{id} ----------

    @Test
    void update_byOwner_returns200AndRecomputesExcerpt() throws Exception {
        long id = createPost("user-1", "tech");

        mvc.perform(put("/api/posts/{id}", id)
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New title","content":"New content","category":"life"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New title")))
                .andExpect(jsonPath("$.category", is("life")))
                .andExpect(jsonPath("$.excerpt", is("New content")))
                .andExpect(jsonPath("$.published", is(true)));
    }

    @Test
    void update_byOtherUser_returns403() throws Exception {
        long id = createPost("user-1", "tech");

        mvc.perform(put("/api/posts/{id}", id)
                        .header("x-user-id", "user-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hack","content":"x","category":"tech"}
                                """))
                .andExpect(status().isForbidden());
    }

    // ---------- DELETE /api/posts/{id} ----------

    @Test
    void delete_byOwner_returns204ThenGetReturns404() throws Exception {
        long id = createPost("user-1", "tech");

        mvc.perform(delete("/api/posts/{id}", id).header("x-user-id", "user-1"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_byOtherUser_returns403() throws Exception {
        long id = createPost("user-1", "tech");

        mvc.perform(delete("/api/posts/{id}", id).header("x-user-id", "user-2"))
                .andExpect(status().isForbidden());
    }

    // ---------- helpers ----------

    private String json(String content, String excerpt) throws Exception {
        var node = objectMapper.createObjectNode()
                .put("title", "T")
                .put("content", content)
                .put("category", "tech");
        if (excerpt != null) {
            node.put("excerpt", excerpt);
        }
        return objectMapper.writeValueAsString(node);
    }

    private long createPost(String userId, String category) throws Exception {
        String body = mvc.perform(post("/api/posts")
                        .header("x-user-id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","content":"C","category":"%s","tags":["a"]}
                                """.formatted(category)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get("id").asLong();
    }
}
