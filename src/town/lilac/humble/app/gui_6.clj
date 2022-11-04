(ns town.lilac.humble.app.gui-6
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.canvas :as canvas]
   [io.github.humbleui.core :as core]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.protocols :as protocols]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.ui :as ui2])
  (:import
   [io.github.humbleui.skija Canvas]
   [io.github.humbleui.types IRect]))

(core/deftype+ Circle [fill stroke ^:mut my-rect]
  protocols/IComponent
  (-measure
   [_ ctx cs]
   cs)

  (-draw
   [this ctx ^IRect rect ^Canvas canvas]
   (set! my-rect rect)
   (let [{:keys [x y right bottom]} rect
         width (- right x)
         height (- bottom y)
         r (/ (min width height) 2)]
     (canvas/draw-circle
      canvas
      ;; TODO scale
      (+ (:x rect) r) (+ (:y rect) r) r
      (or stroke (paint/stroke 0xFF999999 2)))
     (when fill
       (canvas/draw-circle
        canvas
        ;; TODO scale
        (+ (:x rect) r) (+ (:y rect) r) r
        fill))))

  (-event [_ ctx event])

  (-iterate
   [this ctx cb]
   (cb this)))

(defn circle
  [fill stroke]
  (->Circle fill stroke nil))


(defn app
  [{:keys [on-add-circle on-undo on-redo on-select]} *state]
  (ui/default-theme
   {}
   (ui/padding
    10
    (ui/column
     (ui/center
      (ui/row
       (ui/button on-undo (ui/label "Undo"))
       (ui/gap 10 10)
       (ui/button on-redo (ui/label "Redo"))))
     (ui/gap 10 10)
     [:stretch 1
      (ui/clickable
       {:on-click (fn [e] (on-add-circle (- (:x e) 20)
                                         (- (:y e) 20)
                                         (+ (:x e) 20)
                                         (+ (:y e) 20)))}
       (ui/rounded-rect
        {:radius 4}
        (paint/stroke 0xFFCCCCCC 2)
        (ui/row
         (ui/dynamic
          _ctx [circles (:circles @*state)
                selected (:selected @*state)]
          (ui2/stack
           (for [[i c] (map-indexed vector circles)]
             (ui2/absolute
              (select-keys c [:x :y :bottom :right])
              (ui/clickable
               {:on-click (fn [e]
                            (case (:button e)
                              :primary (on-select i)
                              :secondary nil ; on-show-menu
                              nil))}
               (circle
                (when (= selected i)
                  (paint/fill 0xFFDDDDDD))
                nil)))))))))]))))


(defn start!
  []
  (let [*state (atom {:circles [{:x 120 :y 120 :bottom 160 :right 160}]
                      :undo-history '({:x 120 :y 120 :bottom 160 :right 160})
                      :redo-history ()
                      :selected nil})]
    (reset! state/*app (app
                        {:on-add-circle
                         (fn [x y right bottom]
                           (swap!
                            *state
                            (fn [state]
                              (-> state
                                  (update :circles conj {:x x
                                                         :y y
                                                         :right right
                                                         :bottom bottom})
                                  (update :undo-history conj
                                          (:circles state))
                                  (assoc :redo-history ()
                                         :selected nil)))))
                         :on-undo
                         #(swap!
                           *state
                           (fn [state]
                             (if-let [circles (peek (:undo-history state))]
                               (-> state
                                   (assoc :circles circles
                                          :selected nil)
                                   (update :undo-history pop)
                                   (update :redo-history conj (:circles state)))
                               state)))

                         :on-redo
                         #(swap!
                           *state
                           (fn [state]
                             (if-let [circles (peek (:redo-history state))]
                               (-> state
                                   (update :redo-history pop)
                                   (update :undo-history conj circles)
                                   (assoc :circles circles
                                          :selected nil))
                               state)))

                         :on-select (fn [i] (swap! *state assoc :selected i))}
                        *state)))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 700 500)))


(start!)
