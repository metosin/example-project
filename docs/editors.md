# Editors

In general, the required nrepl middleware depends on the used middleware,
so if possible, it is best to not include it in the repo.

TODO: Does this work always? Are there cases where it would be better to
include the config in the repo?

[LSP configuration](../.lsp/config.edn) is included in the repo. It is shared for all editors
that use LSP (Vim, Emacs, VS Code).

## Vim

### [Vim-iced](https://liquidz.github.io/vim-iced/#quick_start)

`iced repl A:backend:dev:repl`

This command will start nREPL server with required middleware (cider, iced)
and using the clj aliases specified in the command.

## Emacs

TODO: [Cider](https://cider.mx/)

## VS Code

TODO: [Calva](https://calva.io/)

## IntelliJ

TODO: [Cursive](https://cursive-ide.com/)
