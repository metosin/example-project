# MUI

## Theme

Something about extending theme if you need some new variants?

## Layout

- RECOMMENDATION: Use flexbox for layout
- Something about using `Stack`

## Popover, popper, menu

- RECOMMENDATION: [material-ui-popup-state](https://github.com/jcoreio/material-ui-popup-state) is
a useful library to handle the state for these components.

## DataGrid

### `:slots`

- **Slot component values should be stable!** Avoid using inline functions, they will
  very likely make the DataGrid to re-initialize itself or the slot components
  on each render. Instead make a top-level `defn` with `uix/as-react`.
  - To pass in extra parameters use `:slotProps` option on `DataGrid`. The value should be
    a JS object, but the object property values could be Cljs data.
  - Another option is to use Context to make some values available to the slot components.
