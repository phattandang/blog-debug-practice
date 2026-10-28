package com.practice.blog.post;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer-only tests: the service is mocked, so these isolate
 * controller wiring (headers, @RequestBody, @Valid, status codes).
 */
@WebMvcTest(PostController.class)
class PostControllerWebMvcTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PostService service;

    @Test
    void create_passesHeaderValueToService() throws Exception {
        when(service.create(any(), eq("user-42"))).thenReturn(new PostResponse(
                1L, "T", "C", "tech", "C", List.of(), null, true, "user-42",
                Instant.now(), Instant.now()));

        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","content":"C","category":"tech"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorId", is("user-42")));

        verify(service).create(any(PostRequest.class), eq("user-42"));
    }

    @Test
    void create_invalidBody_neverReachesService() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(), any());
    }

    @Test
    void create_withoutJsonContentType_returns415() throws Exception {
        mvc.perform(post("/api/posts")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("""
                                {"title":"T","content":"C","category":"tech"}
                                """))
                .andExpect(status().isUnsupportedMediaType());
    }
}
