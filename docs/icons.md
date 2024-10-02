# Icons

## Use existing packages for React component icons

https://www.npmjs.com/package/@mui/icons-material

```
(ns foo
  (:require ["@mui/icons-material/Home$default" :as Home]))

($ Home)
```

## Custom svg files

With Closure-compiler, you can use svgr + Babel to generate
JS files to use from Cljs code.

The repo contains an example for this. Run `bb svg-icons` to run
svgr and babel, the results are committed to the repo.

Relevant configuration:

- `svgr.config.js` configuration for SVGr
    - `replaceAttrValues` replaces color codes so the color can be controlled using
      CSS text color
- `.babelrc` used to convert the JSX from from SVGr to JS that can be consumed by Closure-compiler
- `bb.edn`

Often you'll likely want to wrap the custom icon components with MUI SvgIcon component. This ensures the svg components
follow font size/MUI icon sizing, and has support for `:sx` property.

Require form `/` tells Shadow-cljs to look for JS files in root of project source-paths. `src/js` in included
in `deps.edn` `frontend` alias.

```
(ns foo
  (:require ["/Cross$default" :as Cross]))

($ SvgIcon
   {:component Cross})
```
