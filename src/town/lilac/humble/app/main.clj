(ns town.lilac.humble.app.main
  "The main app namespace.
  Responsible for initializing the window and app state when the app starts."
  (:require
   [io.github.humbleui.ui :as ui]
   ;; [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state])
  (:import
   [io.github.humbleui.skija Color ColorSpace]
   [io.github.humbleui.jwm Window]
   [io.github.humbleui.jwm.skija LayerMetalSkija]))

(def app
  "Main app definition."
  (ui/default-theme ; we must wrap our app in a theme
   {}
   ;; just some random stuff
   (ui/center
    (ui/label "1 2 3"))))

;; reset current app state on eval of this ns
(reset! state/*app app)

;; if you use clojure.tools.namespace, you can comment this out.
;;(state/redraw!)

(defn -main
  "Run once on app start, starting the humble app."
  [& args]
  (ui/start-app!
   (reset! state/*window
           (ui/window
            {:title    "Editor"
             :bg-color 0xFFFFFFFF}
            state/*app)))
  (state/redraw!))
