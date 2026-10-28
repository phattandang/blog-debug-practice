# Solutions (spoilers!)

Only read the entry for the drill you just finished. Each entry covers what you
should have seen, the fastest path to the cause, the fix, and the lesson.

"Tests catch it?" says whether `./check.sh` goes red with the bug in place.

<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>

---

## 01: Wrong header name (frontend)

- **Tests catch it?** Frontend (`api.test.js`).
- **Network tab:** POST → `400`, body `Missing required header: x-user-id`. Request
  Headers show `user-id: user-1`. The header is there, but under the wrong name.
- **Cause:** `frontend/src/api.js` sets `headers['user-id']`.
- **Fix:** `headers['x-user-id'] = userId`.
- **Lesson:** "Missing header" doesn't always mean "not sent". Compare the exact name against the spec.

## 02: Missing Content-Type (frontend)

- **Tests catch it?** Frontend.
- **Network tab:** POST/PUT → `415 Unsupported Media Type`. Request headers show
  `Content-Type: text/plain;charset=UTF-8` (the browser default for a string body).
  DELETE has no body, so it still works.
- **Cause:** the `Content-Type: application/json` line was removed from `api.js`.
- **Fix:** add it back for requests with a body.
- **Lesson:** 415 = the server won't parse the body *type*. 400 = it parsed it and didn't like it.

## 03: Tags sent as a string (frontend)

- **Tests catch it?** Frontend (`postPayload.test.js`).
- **Network tab:** failing requests have `"tags": "java, spring"` (a string). Successful
  ones have no tags at all. Response is `400 Malformed request body`.
- **Cause:** `postPayload.js` no longer splits the comma-separated input into an array.
- **Fix:** restore `form.tags.split(',').map(t => t.trim()).filter(Boolean)`.
- **Lesson:** "Some requests fail" → diff a good payload against a bad one.
  `Malformed request body` (HttpMessageNotReadableException) means a JSON *type* mismatch.

## 04: Missing preventDefault (frontend, browser-only)

- **Tests catch it?** No.
- **Symptom:** the page reloads and the URL becomes `/?title=...&content=...`. In Network, the
  POST shows as `(canceled)` or never appears (tick "Preserve log" to see it).
- **Cause:** `e.preventDefault()` was removed from `handleSubmit` in `PostForm.jsx`, so the
  browser does a native GET form submission.
- **Fix:** put it back as the first line of `handleSubmit`.
- **Lesson:** form fields in the URL bar = native form submit. Turn on "Preserve log".

## 05: Wrong filter query param (frontend)

- **Tests catch it?** Frontend.
- **Network tab:** `GET /api/posts?cat=tech` → `200` with every post. The backend ignores
  unknown params.
- **Cause:** `listPosts` in `api.js` builds `?cat=`.
- **Fix:** `?category=`.
- **Lesson:** a 200 isn't proof of correctness. Check the URL against the spec. Spring
  silently ignores params it doesn't bind.

## 06: List not refreshed after save (frontend, browser-only)

- **Tests catch it?** No.
- **Network tab:** POST → `201` (or PUT → `200`) with the correct body, then nothing. No
  `GET /api/posts` follows. Changing the filter or reloading shows the new data.
- **Cause:** the trailing `await refresh()` was removed from `handleSubmit` in `App.jsx`.
- **Fix:** call `await refresh()` after the create/update.
- **Lesson:** backend correct + UI stale = a client state/refresh bug. Ask "which request
  *should* have happened next?" and look for it in the Network tab.

## 07: CORS doesn't allow x-user-id (backend, browser-only)

- **Tests catch it?** No (MockMvc doesn't do CORS preflight unless you test it explicitly).
- **Network tab:** an `OPTIONS` preflight to `/api/posts` returns `403`. The POST itself
  shows `CORS error`. Console: *"Request header field x-user-id is not allowed by
  Access-Control-Allow-Headers in preflight response."* GET works because it has no custom
  header, so it's a "simple" request with no preflight.
- **Cause:** `CorsConfig.java` → `.allowedHeaders("Content-Type")`.
- **Fix:** `.allowedHeaders("Content-Type", "x-user-id")`.
- **Lesson:** "works in curl, fails in browser" = CORS almost every time. curl doesn't do CORS.
  Read the Console, not just the Network tab.

## 08: Missing @RequestBody (backend)

- **Tests catch it?** Backend.
- **Network tab:** the request payload is perfect. Response: `400 Validation failed`, every
  required field in `details`.
- **Cause:** `PostController.create` has `@Valid PostRequest request` without `@RequestBody`,
  so Spring binds from query params (none) and every field is null.
- **Fix:** `@Valid @RequestBody PostRequest request`.
- **Lesson:** "server says field is missing but I sent it" → the server isn't reading the
  body. Compare create with update, since update works.

## 09: Missing @Valid (backend)

- **Tests catch it?** Backend (`create_blankRequiredFields...`, `create_invalidBody_neverReachesService`).
- **Symptom:** blank fields are accepted. A null `category` hits the DB `NOT NULL` constraint →
  `500` (`DataIntegrityViolationException` in the server log).
- **Cause:** `@Valid` removed from `create`.
- **Fix:** add it back.
- **Lesson:** `@NotBlank` does nothing without `@Valid` on the parameter. A 500 from the DB
  layer usually means validation never ran.

## 10: published not set (backend)

- **Tests catch it?** Backend.
- **Network tab:** create response has `"published": false`.
- **Cause:** `post.setPublished(true)` removed from `PostService.create`. A `boolean` field
  defaults to `false`.
- **Fix:** put it back.
- **Lesson:** spec lines like "always true" are easy to drop. Check every field of the response.

## 11: Excerpt off by one (backend)

- **Tests catch it?** Backend (`excerpt_contentLongerThan150...`, `create_autoExcerpt...`).
- **Test output:** `expected 150 but was 149`, or a jsonPath mismatch on `$.excerpt`.
- **Cause:** `content.substring(0, EXCERPT_LENGTH - 1)` in `buildExcerpt`.
- **Fix:** `substring(0, EXCERPT_LENGTH)`. `substring`'s end index is exclusive.
- **Lesson:** read the assertion message. The fastest tests to read are the plain unit tests
  (`PostServiceTest`), so look there first.

## 12: Blank excerpt kept (backend)

- **Tests catch it?** Backend (`create_blankExcerptIsTreatedAsMissing`, `excerpt_blankExcerpt...`).
- **Why the UI hides it:** our React form already omits a blank excerpt, so you have to
  reproduce with curl:
  ```bash
  curl -i -X POST localhost:8080/api/posts -H 'Content-Type: application/json' \
    -H 'x-user-id: u1' -d '{"title":"t","content":"hello","category":"tech","excerpt":""}'
  ```
- **Cause:** `buildExcerpt` checks `excerpt != null` but not `isBlank()`.
- **Fix:** `if (excerpt != null && !excerpt.isBlank())`.
- **Lesson:** the backend must enforce the spec on its own. Never rely on "our frontend doesn't send that".

## 13: 200 instead of 201 (backend)

- **Tests catch it?** Backend.
- **Network tab:** create returns `200 OK`.
- **Cause:** `@ResponseStatus(HttpStatus.CREATED)` removed from `create`.
- **Fix:** put it back (or return `ResponseEntity.status(201).body(...)`).
- **Lesson:** status codes are part of the contract, and graders check them.

## 14: Bad derived query name (backend, startup failure)

- **Tests catch it?** Backend. **Every** `@SpringBootTest` errors, which looks scary.
- **Reading the output:** scroll to the *first* `Caused by:` chain and go to the deepest one:
  `No property 'name' found for type 'String'; Traversed path: Post.category`, from
  `Failed to create query for method ... findByCategoryNameIgnoreCase...`.
  (`PostServiceTest` still passes, since it has no Spring context. That's a clue too.)
- **Cause:** the repository method was renamed to `findByCategoryName...`, but `Post` has
  no `categoryName` property.
- **Fix:** rename back to `findByCategoryIgnoreCaseOrderByCreatedAtDesc` (repo + service).
- **Lesson:** "everything fails at once" = context won't start. Find the root `Caused by`, not the 30 test errors.

## 15: LazyInitializationException (backend)

- **Tests catch it?** Backend (`list_*` tests).
- **Network tab:** `GET /api/posts` → `500`. Server log:
  `LazyInitializationException: failed to lazily initialize a collection of role: ...Post.tags ... no Session`.
- **Cause:** `@Transactional(readOnly = true)` removed from `PostService.list`. `tags` is a lazy
  `@ElementCollection`, and with `spring.jpa.open-in-view=false` there's no session left when
  `PostResponse.from` reads it.
- **Fix:** put `@Transactional(readOnly = true)` back. (Alternatives: a fetch join or
  `@EntityGraph`.)
- **Lesson:** a 500 means the answer is in the **server console**, not the browser. Look for the first stack trace.

## 16: Ownership check inverted (backend)

- **Tests catch it?** Backend.
- **Network tab:** PUT/DELETE as the author → `403`. As another user → `200`/`204`.
- **Cause:** `checkOwner` lost its `!`.
- **Fix:** `if (!post.getAuthorId().equals(userId))`.
- **Lesson:** reproduce with two users (change "Acting as user" in the header) to see the inversion.

## 17: Tags dropped in response mapping (backend)

- **Tests catch it?** Backend.
- **Narrowing down:** the request has `"tags": ["a","b"]`, the response has `"tags": []`. Saved
  or not? Check the H2 console: `SELECT * FROM POST_TAGS;` The rows are there, so the
  bug is on the way *out*.
- **Cause:** `PostResponse.from` uses `List.of()` instead of `List.copyOf(post.getTags())`.
- **Fix:** map the real tags.
- **Lesson:** split "not saved" from "not returned" by looking at the database directly.
  Request DTO → entity → response DTO: check each hop.

## 18: Wrong API port (frontend)

- **Tests catch it?** Frontend.
- **Network tab:** requests go to `localhost:8081` and fail with `ERR_CONNECTION_REFUSED`.
  No status code, and no OPTIONS request either.
- **Cause:** `API_URL` default in `api.js` is `http://localhost:8081`.
- **Fix:** `8080` (or set `VITE_API_URL` in `frontend/.env.local`).
- **Lesson:** "Failed to fetch" has several causes. Check the request URL first. Connection
  refused (wrong host/port, server down) looks different from a CORS error (server answered,
  the browser blocked it). Compare with drill 07.
