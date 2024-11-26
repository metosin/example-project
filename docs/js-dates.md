# JS date libs

https://mui.com/x/react-date-pickers/base-concepts/#date-library

## https://github.com/js-joda/js-joda

Port of JSR-310 i.e. the Java.time implementation, so this has all the
same types. Pure implementation, not over JS Date.

Cons:

- Need to require through `@js-joda/core` so we always get all the classes
  even if app would only need `Instant` or something. Due to static property
  initialization and dependencies between classes it looks like it isn't possible
  to only require single class (at least easily?).
- 43 kB minified and compressed (advertiser), not huge, but more than many other
  date libs
- No MUI DatePicker adapter (could be added?)

## https://day.js.org/en/

- 2kB Moment.js modern alternative, same API
- MUI DatePicker adapter

- https://date-fns.org/
- https://moment.github.io/luxon/#/
