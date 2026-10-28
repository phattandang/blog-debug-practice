# Blog Debug Practice

A small, **working** full-stack app (Spring Boot + React) with a built-in bug
injector. You don't build features here. You practice the debugging loop until
it's automatic.

```
./break.sh        # plants a random bug and prints a bug ticket, starts a timer
...debug...
./done.sh         # runs all tests, stops the timer, logs your time
./reset.sh        # back to the clean baseline for the next drill
```

## Requirements

- Java 17+ (Maven is **not** required: use the bundled `./mvnw`)
- Node 18+
- Git, plus a bash shell (macOS/Linux terminal, or **Git Bash** on Windows)

## First-time setup

```bash
cd frontend && npm install && cd ..
./check.sh        # should print "passed" twice
```

## Running the app

Two terminals:

```bash
# terminal 1: API on http://localhost:8080
cd backend && ./mvnw spring-boot:run
```

```bash
# terminal 2: UI on http://localhost:5173
cd frontend && npm run dev
```

H2 database console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:blogdb`, user `sa`, no password).
It's handy for answering "did it actually get saved?"

**After `./break.sh` or `./reset.sh`, restart the backend** (Ctrl+C, run again).
Vite hot-reloads the frontend on its own.

## The drill loop

1. `./break.sh` (or `./break.sh 7` for a specific one, `./break.sh hard` for two bugs at once).
2. Read the ticket. It describes what a user or teammate saw, not the cause.
3. Debug in this order, every time:
   1. **Reproduce.** Do what the ticket says. Open DevTools → Network. Look at the
      request (URL, method, headers, payload) and the response (status, body).
   2. **Frontend or backend?** Compare the request against [SPEC.md](SPEC.md).
      - Request is wrong → frontend bug.
      - Request matches the spec but the response is wrong → backend bug.
      - Request never shows up, or shows as `(failed)` / `CORS error` → look at the browser
        Console, and think about the port and CORS.
   3. **Run the tests**: `./check.sh`, or `cd backend && ./mvnw test -e`.
      Read the **first** failure, not the last line of Maven output.
      Surefire reports are in `backend/target/surefire-reports/`.
   4. **Fix one thing, rerun, repeat.**
4. `./done.sh` when the tests pass **and** you've confirmed the fix in the browser.
   Some bugs are invisible to the test suite on purpose.
5. Read the drill's entry in [drills/SOLUTIONS.md](drills/SOLUTIONS.md) and compare it
   with how you found it. Was there a faster path?
6. `./reset.sh`

Your history is kept in `drills/log.csv` (date, drill, seconds). Watch the times drop.

## Rules (so it stays useful)

- Don't open `drills/patches/` or `drills/SOLUTIONS.md` until you're done.
- Don't use `git diff main`, `git log -p` or `git show` during a drill. That's reading the
  answer key. Plain `git diff` is fine: it shows only *your* edits.
- No AI help during a timed run.
- Set a timebox (say 20 minutes). If you hit it, look at the solution, then
  redo the same drill later with `./break.sh <n>`.

## Where things live

```
backend/src/main/java/com/practice/blog/
  post/PostController.java     REST endpoints, @RequestHeader, @Valid
  post/PostService.java        business rules (excerpt, published, ownership)
  post/PostRequest.java        request DTO + validation annotations
  post/PostResponse.java       response DTO + entity mapping
  post/Post.java               JPA entity
  post/PostRepository.java     Spring Data queries
  common/GlobalExceptionHandler.java   clean 400/403/404 JSON errors
  common/CorsConfig.java       lets the React dev server call the API
backend/src/test/...           @SpringBootTest, @WebMvcTest and plain unit tests

frontend/src/
  api.js                       fetch wrapper: URL, headers, error handling
  postPayload.js               form strings -> API JSON (tags array, readTime number)
  components/PostForm.jsx      controlled form, shows server errors
  components/PostList.jsx      list + category filter
  App.jsx                      wires it together
```

## Leveling up

Once the 18 drills are boring:

- Write your own drill: break something, then
  `git diff > drills/patches/19.patch`, `git checkout -- .`, and add a line to `drills/tickets.txt`.
- Add a feature under a timer (60 minutes): pagination on `GET /api/posts`, a `PATCH` to
  unpublish, or a search by tag. Write the spec checklist first.
