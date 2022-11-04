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
    ;; TODO scale
    :height (* 2 (:radius opts))
    :width (* 2 (:radius opts))))

  (-draw
   [this ctx ^IRect rect ^Canvas canvas]
   (set! my-rect rect)
   (let [r (:radius opts)]
     (canvas/draw-circle
      canvas
      ;; TODO scale
      (+ (:x rect) r) (+ (:y rect) r) r
      (paint/stroke 0xFFAAAAAA 2))))

  (-event [_ ctx event])

  (-iterate
   [this ctx cb]
   (cb this)))

(defn circle
  [{:keys [on-click radius] :as opts}]
  (ui/clickable
   {:on-click (when on-click (fn [_] (on-click)))}
   (->Circle opts nil)))


(core/deftype+ AbsolutePosition [opts child ^:mut my-rect]
  protocols/IComponent
  (-measure
   [_ ctx cs]
   (core/measure child ctx cs))

  (-draw
   [_ ctx ^IRect rect ^Canvas canvas]
   (set! my-rect rect)
   (core/draw-child
    child ctx
    (assoc rect
           :x (:x opts)
           :y (:y opts))
    canvas))

  (-event
   [_ ctx event]
   (core/event-child child ctx event))

  (-iterate
   [this ctx cb]
   (or
    (cb this)
    (protocols/-iterate child ctx cb))))

(defn absolute
  [{:keys [x y] :as opts} child]
  (->AbsolutePosition opts child nil))


(defn stack
  [children]
  (apply ui/stack children))

(defn circles
  [{:keys [on-add-circle]} *state]
  (ui/default-theme
   {}
   (ui/padding
    10
    (ui/column
     (ui/center
      (ui/row
       (ui/button nil (ui/label "Undo"))
       (ui/gap 10 10)
       (ui/button nil (ui/label "Redo"))))
     (ui/gap 10 10)
     [:stretch 1
      (ui/clickable
       {:on-click (fn [e] (on-add-circle (:x e) (:y e)))}
       (ui/rounded-rect
        {:radius 4}
        (paint/stroke 0xFFCCCCCC 2)
        (ui/row
         (ui/dynamic
          _ctx [circles (:circles @*state)]
          (stack
           (for [c circles]
             (absolute
              (select-keys c [:x :y])
              (circle {:radius (:r c)
                       :on-click #(prn "hi")}))))))))]))))


(defn start!
  []
  (let [*state (atom {:circles [{:x 100 :y 100 :r 10}]})]
    (reset! state/*app (circles
                        {:on-add-circle
                         (fn [x y]
                           (swap! *state update :circles conj {:x x
                                                               :y y
                                                               :r 20}))}
                        *state)))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 700 500)))


(start!)
