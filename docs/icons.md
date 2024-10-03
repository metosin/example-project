# Icons

## Use existing packages for React component icons

https://www.npmjs.com/package/@mui/icons-material

```cljs
(ns foo
  (:require ["@mui/icons-material/Home$default" :as Home]))

($ Home)
```

## Custom svg files

This isn't strictly just for "icons". You could use this for other
SVG artwork also.

With Closure-compiler, you can use svgr + Babel to generate
JS files to use from Cljs code.

The repo contains an example for this. Run `bb icons` to run
svgr and babel, the results are committed to the repo.

Relevant configuration:

- `svgr.config.js` configuration for SVGr
    - `replaceAttrValues` replaces color codes in svg elements so the color can
      be controlled using CSS text color (`currentColor`). You could also replace
      the colors with CSS variables like `var(--icon-color-1)` if you need to be
      able to control multiple colors in the icons.
- `.babelrc` used to convert the JSX from from SVGr to JS that can be consumed by Closure-compiler
- `bb.edn`

Often you'll likely want to wrap the custom icon components with MUI SvgIcon component. This ensures the svg components
follow font size/MUI icon sizing, and has support for `:sx` property.

Require form with a string starting with `/` tells Shadow-cljs to look for JS
files in root of project source-paths. The folder `src/js` in included
in `deps.edn` `frontend` alias.

```cljs
(ns foo
  (:require ["/icons/Cross$default" :as Cross]))

($ SvgIcon
   {:component Cross})
```

### Alternative tooling

If you use ESBuild or other JS bundler, you don't need to precompile the svgs
to JS files, and instead can let the bundler to handle the files:
https://github.com/metosin/shadow-cljs-esbuild
