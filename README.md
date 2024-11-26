# Opinionated example project

Confidence level explanation:
- Very high, high: I strongly believe this is the best choice nearly always
- Medium: Should be a good choice, there could be cases where other choices are better
- Low: I'm not sure if this is a good choice, but it solves something

## Backend choices

| Lib/feature | Confidence level | Comments |
|---|---|---|
| Integrant | Very high | |
| Reitit | Very high | |
| Malli | Very high | |
| Next.jdbc | Very high | |
| Honeysql v2 | High | Can be useful to generate dynamic SQL queries |
| HugSQL | High | Can be useful when writing SQL by hand |
| hikari-cp | High | |
| Log4j2/Logback | Medium | Current favorite is Log4j2. Logback is also great (and is used in most projects already,) |
| ring-jetty-adapter | | ring/ring-jetty-adapter should be good now or http-kit? |
| Migratus/Flyway | High | Migratus is a good choice. Flyway has been used in many projects so there might be more experience of using it at Metosin. |
| Java.time | High | No need for joda-time |
| [Kaocha](./docs/test.md) | Medium | Kaocha for Clj test runner |

## Frontend choices

| Lib/feature | Confidence level | Comments |
|---|---|---|
| [UIx](./docs/uix.md) | High | Simple React wrapper makes it easier to use modern React and libs |
| [Re-frame](./docs/re-frame.md) | Medium | Even though Re-frame is for Reagent, it is usable with UIx and we have considerable experience using it |
| [MUI](./docs/mui.md) | Medium? | One solution to writing styles inline in the Cljs components, seems to work relatively well with Cljs |
| [Icons](./docs/icons.md) | | Use JS libs and svg files (for custom icons) |
| Reitit-frontend | Medium? | Good enough? It is used in most projects already. |
| Malli | High | If/when needed for FE route parameter validation or any other schema validations. |
| [Date lib? Js-joda? Day-js or something?](./docs/js-dates.md) | Medium? | Moment is kind of old now. Closure Library is deprecated. |
| MAYBE: TanStack Query | Low? Medium? | Useful abstraction for making calls to backend. Some challenges on using this together with Re-frame (and some with Cljs, but mostly solved.) |
| [Test runner](./docs/test.md) | Medium | Shadow-cljs can build unit test artifact, run in browser or with Karma |

Open questions?
- Forms
- Alternative component libs, no experience. Both use Emotion like MUI? So similar `sx` props.
    - Chakra UI
    - Theme UI

## Code style

- [Clj-kondo config](./.clj-kondo/config.edn)
- [Clojure-LSP config](./.lsp/config.edn)

## Deployment

- Create uberjar: `bb build`
- Create docker image: `docker build .`
