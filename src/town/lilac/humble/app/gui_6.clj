(ns town.lilac.humble.app.gui-6
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.canvas :as canvas]
   [io.github.humbleui.core :as core]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.protocols :as protocols]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state])
  (:import
   [io.github.humbleui.skija Canvas]
   [io.github.humbleui.types IRect]))

(core/deftype+ Circle [opts ^:mut my-rect]
  protocols/IComponent
  (-measure
   [_ ctx cs]
   (assoc
    cs
    :height (* 2 (:radius opts))
    :width (* 2 (:radius opts))))
  (-draw
   [this ctx ^IRect rect ^Canvas canvas]
   (set! my-rect rect)
   (let [r (:radius opts)]
     (canvas/draw-circle
      canvas
      (+ (:x rect) r) (+ (:y rect) r) r
      (paint/stroke 0xFFAAAAAA 2))))
  (-event [_ ctx event])
  (-iterate [_ ctx cb]))

(defn circle
  [opts]
  (->Circle opts nil))

(defn circles
  [*state]
  (ui/default-theme
   {}
   (ui/padding
    10
    (ui/column
     (ui/center
      (ui/row
       (ui/button nil (ui/label "Undo"))
       (ui/button nil (ui/label "Redo"))))
     (ui/gap 10 10)
     [:stretch 1
      (ui/rounded-rect
       {:radius 4}
       (paint/stroke 0xFFCCCCCC 2)
       (ui/row
        (ui/dynamic
         _ctx [state @*state]
         (circle {:radius 50}))))]))))


(defn start!
  []
  (reset! state/*app (circles (atom {})))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 700 500)))


(start!)
