# JS libs

Just install with `npm install` and require in the Cljs ns form

## Requires

ESM import:
`import defaultExport from "module-name";`

Cljs require:
`(:require ["module-name$default" :as defaultExport])`

(`$default` is the Cljs way to say use the `.default` property from the file)

https://shadow-cljs.github.io/docs/UsersGuide.html#_using_npm_packages

- Prefer importing individual files/React components vs. index.js
  - Closure optimization/DCE works only on level of requires JS files for
    js libs, not the contents of JS libs, so if you require Material-UI index.js,
    you get all of the MUI to your output
