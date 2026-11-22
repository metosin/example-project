# JS date libs

https://mui.com/x/react-date-pickers/base-concepts/#date-library

## [date-fns](https://date-fns.org/)

Used in this project: `frontend.http-fx` reads transit `Instant` values
(`java.time.Instant` from the backend) as JS `Date` objects using `parseISO`.

- Simple API, pure functions, immutable.
- Works on plain JS `Date` objects, so there are no own types: an instant is
  just a `Date` with millisecond precision (microseconds from e.g. Postgres
  timestamps are truncated).
- Locales and formatting seem to work.
- MUI DatePicker adapter (`AdapterDateFns`).
- Require each function from its own module, e.g.
  `["date-fns/parseISO" :refer [parseISO]]`, not `["date-fns" :refer [parseISO]]`.
  Shadow-cljs doesn't tree-shake npm code, so requiring the package root pulls
  in the whole library.

Shadow-cljs release build size of the example app (`npx shadow-cljs release app`,
`main.js`, gzip with `gzip -9`, measured 2026-11):

| Setup | main.js | gzip |
|---|---|---|
| `@js-joda/core` (`Instant/parse`) | 958 kB | 242 kB |
| date-fns 4, `date-fns/parseISO` | 766 kB | 202 kB |
| date-fns 4, `parseISO` + `format` + `fi` locale | 797 kB | 210 kB |
| date-fns 4, `parseISO` required from package root `date-fns` | 914 kB | 228 kB |

According to the shadow-cljs build report
(`npx shadow-cljs run shadow.cljs.build-report app report.html`) js-joda was
195 kB of the optimized output (20 % of the bundle), date-fns `parseISO` is
4 kB.

## https://github.com/js-joda/js-joda

Port of JSR-310 i.e. the Java.time implementation, so this has all the
same types. Pure implementation, not over JS Date.

Cons:

- Need to require through `@js-joda/core` so we always get all the classes
  even if app would only need `Instant` or something. Due to static property
  initialization and dependencies between classes it looks like it isn't possible
  to only require single class (at least easily?).
- 43 kB minified and compressed (advertised in readme). With shadow-cljs the
  whole `@js-joda/core` was 195 kB of the optimized output (~40 kB gzipped),
  see the table above.
- No MUI DatePicker adapter (could be added?)

## https://day.js.org/en/

- 2kB Moment.js modern alternative, same API
- MUI DatePicker adapter

- https://date-fns.org/
- https://moment.github.io/luxon/#/
