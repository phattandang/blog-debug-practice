# API Spec Checklist

Each line is tested in `backend/src/test/.../PostApiTest.java`.
When debugging, compare what the Network tab shows against this list.

## Common

- [ ] Base URL `http://localhost:8080`
- [ ] JSON in and out: requests with a body must send `Content-Type: application/json`
- [ ] Errors look like `{"status": 400, "error": "message", "details": {"field": "message"}}`
- [ ] The browser origin `http://localhost:5173` is allowed (CORS), including the `x-user-id` header

## `POST /api/posts` (create)

- [ ] Header `x-user-id` is **required**. Missing → `400`, `error` = `Missing required header: x-user-id`
- [ ] `title` required, not blank
- [ ] `content` required, not blank
- [ ] `category` required, not blank
- [ ] Any required field missing or blank → `400`, `error` = `Validation failed`, `details.<field>` = `<field> is required`
- [ ] `excerpt` optional. Missing **or blank** → the first **150** characters of `content` (all of it if shorter)
- [ ] `tags` optional, a JSON **array** of strings. Missing or `null` → `[]`
- [ ] `readTime` optional, a JSON **number** (minutes, ≥ 0). Non-numeric → `400` `Malformed request body`
- [ ] `published` is **always `true`**, even if the client sends `false`
- [ ] `authorId` is set from the `x-user-id` header
- [ ] Success → **`201 Created`** with the full post

Response shape:

```json
{
  "id": 1,
  "title": "Hello",
  "content": "Some content",
  "category": "tech",
  "excerpt": "Some content",
  "tags": ["java", "spring"],
  "readTime": 5,
  "published": true,
  "authorId": "user-1",
  "createdAt": "2026-01-01T12:00:00Z",
  "updatedAt": "2026-01-01T12:00:00Z"
}
```

## `GET /api/posts`

- [ ] Returns `200` with an array of posts, newest first
- [ ] Optional query param `?category=tech` filters by category, **case-insensitive**

## `GET /api/posts/{id}`

- [ ] `200` with the post, or `404` with `error` = `Post {id} not found`

## `PUT /api/posts/{id}`

- [ ] Header `x-user-id` required. Same body rules as create (excerpt is recomputed)
- [ ] Only the author may update → otherwise `403`
- [ ] Unknown id → `404`. Success → `200` with the updated post

## `DELETE /api/posts/{id}`

- [ ] Header `x-user-id` required. Only the author may delete → otherwise `403`
- [ ] Unknown id → `404`. Success → `204 No Content` with an empty body
