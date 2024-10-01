# Opinionated example project

Confidence level explanation:
- Very high, high: I strongly believe this is the best choice nearly always
- Medium: Should be a good choice, there could be cases where other choices are better
- Low: I'm not sure if this is a good choice, but it solves something

## Backend choices

| Lib | Confidence level | Comments |
|---|---|---|
| Integrant | Very high | |
| Reitit | Very high | |
| Malli | Very high | |
| Next.jdbc | Very high | |
| Honeysql v2 | High | Can be useful to generate dynamic SQL queries |
| HugSQL | High | Can be useful when writing SQL by hand |
| hikari-cp | High | |
| Log4j2/Logback | Medium | Current favorite is Log4j2. Logback is also great (and is used in most projects already,) |
| TODO: Http server | | ring/ring-jetty-adapter should be good now or http-kit? NOT: Undertow, Aleph |
| Migratus/Flyway | High | Migratus is a good choice. Flyway has been used in many projects so there might be more experience of using it at Metosin. |
| Java.time | High | No need for joda-time |

## Frontend choices

| Lib | Confidence level | Comments |
|---|---|---|
| UIx | High | Simple React wrapper makes it easier to use modern React and libs |
| Re-frame | Medium | Even though Re-frame is for Reagent, it is usable with UIx and we have considerable experience using it |
| MUI | Medium? | One solution to writing styles inline in the Cljs components, seems to work relatively well with Cljs |
| Reitit-frontend | Medium? | Good enough? It is used in most projects already. |
| Malli | High | If/when needed for FE route parameter validation or any other schema validations. |
| Date lib? Js-joda? Day-js or something? | Medium? | Moment is kind of old now. Closure Library is deprecated. |
| MAYBE: TanStack Query | Low? Medium? | Useful abstraction for making calls to backend. Some challenges on using this together with Re-frame (and some with Cljs, but mostly solved.) |

Open questions?
- Forms
- Alternative component libs, no experience. Both use Emotion like MUI? So similar `sx` props.
    - Chakra UI
    - Theme UI

## "Code style etc"

- Clj-kondo config
- Maybe Clojure-LSP config (if needed)

## Deployment

Project requirements?
- Will run on one node?
- No scaling?

Targets for the example project?
- Produce usable docker image for the backend
- Write optimize JS file somewhere? Index.html with hashed filenames for JS?
- Should JS file be included in the BE image? Might be OK for this case.
  - If the Clj BE serves the FE files, we can generate index.html on the fly with Hiccup and handle hashed filenames there.
- Github Actions example? Could be useful as a starting point even if project will use different CI
