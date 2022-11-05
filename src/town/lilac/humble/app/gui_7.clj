(ns town.lilac.humble.app.gui-7
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.ui :as ui2]))

(defn app
  []
  (ui/default-theme
   {:hui.text-field/border-radius 0
    :hui.text-field/fill-bg-inactive (paint/fill 0xFFFFFFFF)}
   (ui/focus-controller
    (ui/vscrollbar
     (ui/vscroll
      (ui/column
       (ui/row
        (ui/gap 26 20)
        (for [j (map (comp str char) (range 65 91))]
          (ui/width
           80
           (ui/center
            (ui/label j)))))
       (for [i (range 100)]
         (ui/row
          (ui/width
           25
           (ui/valign
            0.5
            (ui/halign
             0.3
             (ui/label i))))
          (for [j (map char (range 26))]
            (ui/column
             (ui/width
              80
              (ui/text-field (atom {})))))))))))))


(defn start!
  []
  (reset! state/*app (app))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 1000 700)))


(start!)
