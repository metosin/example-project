# Editors

In general, the required nrepl middleware depends on the used editor,
so if possible, it is best to not include it in the repo. Or else you
might have to add all the different configuration combinations to the repo.

(OK, including the iced nrepl middleware shouldn't affect Cider and others
so one nrepl alias with all the middleware might work?)

TODO: Does this work always? Are there cases where it would be better to
include the config in the repo?

[LSP configuration](../.lsp/config.edn) is included in the repo. It is shared for all editors
that use LSP (Vim, Emacs, VS Code).

## Vim

### [Vim-iced](https://liquidz.github.io/vim-iced/#quick_start)

Start the nREPL process and the app in a separate terminal (or in a terminal
buffer Neovim if you want to).

`iced repl A:backend:dev:repl`

This command will start a nREPL server with the required middleware (cider, iced)
and using the clj aliases specified in the command. Iced will then connect
to this nREPL using `.nrepl-port` file.

## Emacs

### [Cider](https://cider.mx/)

Add `.dir-locals.el` [Dir locals](https://www.gnu.org/software/emacs/manual/html_node/emacs/Directory-Variables.html)

```elisp
((nil . ((cider-clojure-cli-aliases . ":backend:dev:repl")
         (cider-default-cljs-repl . shadow)
         (cider-shadow-default-options . ":app")
         (cider-shadow-cljs-watched-builds . ("app")))))
```

Then start the cljs build with `cider-jack-in-cljs` using shadow-cljs
and `cider-jack-in-clj` using clojure-cli.

## VS Code

TODO: [Calva](https://calva.io/)

## IntelliJ

TODO: [Cursive](https://cursive-ide.com/)
