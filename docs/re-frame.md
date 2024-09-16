# Re-frame

- Hmm?
- Hooks vs re-frame?
  - Hooks for component local state (for example: is a dropdown open)
  - Re-frame works well for "application state"
  - But it isn't this simple...
- Avoid using `app-db` as input in every sub?
- Use signals fns (instead of `:<-` shortcut) if needed
- Use `reg-sub-raw` if needed?
