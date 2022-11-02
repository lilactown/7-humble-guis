(ns town.lilac.humble.app.gui-1
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]))

(def *count (atom 0))

(defn counter
  [*count]
  (ui/default-theme
   {}
   (ui/center
    (ui/row
     (ui/dynamic
      _ctx [count @*count]
      (ui/center
       (ui/label count)))
     (ui/gap 20 0)
     (ui/button #(swap! *count inc) (ui/label "Count"))))))


(defn start!
  []
  (reset! state/*app (counter (atom 0)))
  (state/redraw!)
  (app/doui
   (window/set-window-position @state/*window 860 566)
   (window/set-content-size @state/*window 300 100)))


(start!)
