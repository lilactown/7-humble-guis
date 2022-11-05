(ns town.lilac.humble.app.gui-7
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.ui :as ui2]))

(defn app
  []
  (ui2/with-theme
    (ui/vscrollbar
     (ui/vscroll
      (ui/column
       (for [i (range 100)]
         (ui/row
          (for [j (range 26)]
            (ui/padding
             1
             (ui/column
              (ui/width 50 (ui/text-field (atom {})))))))))))))


(defn start!
  []
  (reset! state/*app (app))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 1000 700)))


(start!)
