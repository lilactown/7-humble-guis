# humblu-starter

A commented, minimal example project to get you started with developing desktop
apps using [HumbleUI](https://github.com/HumbleUI/HumbleUI) and Clojure.

## Devolepment

To simply run the app, execute `./scripts/run.sh`.

To start a REPL (including a minimal nREPL server), you can run `./scripts/nrepl.sh`

### Reloading

To reload the app and see your changes reflected, you can:

1. Evaluate individual forms via the REPL, reset the `state/*app` atom, and then
   call `state/redraw!`
2. Make changes to the files, save them, then call `reload` from the user ns,
   which will use [clojure.tools.namespace](https://github.com/clojure/tools.namespace)
   to detect which ns' should be refreshed, evaluate them, and then call
   `state/redraw!`.

### CIDER

If you use an editor like Emacs or Calva which integrates using CIDER, you can
customize the jack-in command to work with your HumbleUI app.

> NOTE: The default jack-in command will not work, since we need to start the
> HumbleUI app on a different thread than the nREPL server. By default, the
> nREPL server will start and then you would evaluate commands via this
> connection, but this will not work when starting the HumbleUI app.

To ensure that you are loading the correct version of nREPL and CIDER, we start
by running the jack-in command but customizing it. In Emacs, this is
`C-u M-x cider-jack-in`. An example of what the default command looks like:

```
/opt/homebrew/bin/clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "1.0.0"} cider/cider-nrepl {:mvn/version "0.28.6"}} :aliases {:cider/nrepl {:main-opts ["-m" "nrepl.cmdline" "--middleware" "[cider.nrepl/cider-middleware]"]}}}' -M:cider/nrepl
```

For our purposes, the `user` ns has a `-main` function which handles all of the
app and nREPL server initialization. The only thing we need to do is pass in the
middlewares to it.

Below, we show the command after we delete the use of the `cider/nrepl` alias
and replace it with the `:dev` alias and then run the main function in the
`user` namespace, passing in the nREPL options for CIDER's middleware.

```
/opt/homebrew/bin/clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "1.0.0"} cider/cider-nrepl {:mvn/version "0.28.6"}} :aliases {:cider/nrepl {:main-opts ["-m" "nrepl.cmdline" "--middleware" "[cider.nrepl/cider-middleware]"]}}}' -M:dev -m user --middleware '[cider.nrepl/cider-middleware]'
```
