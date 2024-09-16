# UIx

https://pitch-io.github.io/uix/docs/

## Properties

- DIFFERENCE: UIx doesn't do recursive transformation of component properties to JS like Reagent
  - This is a good feature for performance and usability (so you are able to send Clj data into components when needed)
  - But it means when you need to use JS objects are values of component properties, you need to use `#js` (or other ways) to
    create the values.
  - TODO: `:sx` property values can be in most cases validated with a custom Clj-kondo linter
- RECOMMENDATION: When using React components from JS libs (like MUI) prefer writing the properties
  in `camelCase`
  - This way your code matches the examples of the JS libs
  - Though UIx does snake-case to camelCase tranform for component top-level properties, it doesn't affect
    property values built with `#js` like you would often do with `:sx` values.

## Hooks

https://pitch-io.github.io/uix/docs/hooks.html

- RECOMMENDATION: Prefer UIx hook wrappers to calling React directly
  - Enables UIx linting
  - Will coerce `nil` return values to `js/undefined` for effects hook
- NOTICE: React compares hook dependency values with JS `===` / `Object.is`
  - If you use Cljs data structures as dependencies, their identity can (and often will) be different
    even if they are equal (according to Cljs `=`)
  - UIx hook wrappers stringifies keywords, symbols and UUIDs
