# Tests

- Clojure JVM unit tests
  - Kaocha
  - Run with `bb test`
  - Needs the database from `compose.yaml` (`docker compose up -d`) and the
    test template database: `bb init-test-db` (again after adding a migration)
- ClojureScript unit tests
  - Shadow-cljs browser runner, for locally running these while developing
    `bb test-browser` and open `http://localhost:8081/`
  - Karma for running in headless Chrome, e.g., for CI
    `bb test-karma` (single run)
    - Could be setup for watch & run on change, no example currently
