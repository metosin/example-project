# Tests

- Clojure JVM unit tests
  - Kaocha
  - Run with `bb test`
- ClojureScript unit tests
  - Shadow-cljs browser runner, for locally running these while developing
    `bb test-browser` and open `http://localhost:8081/`
  - Karma for running in headless Chrome, e.g., for CI
    `bb test-karma` (single run)
    - Could be setup for watch & run on change, no example currently
