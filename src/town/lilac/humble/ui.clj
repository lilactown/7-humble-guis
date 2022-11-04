(ns town.lilac.humble.ui
  (:require
   [io.github.humbleui.core :as core]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.protocols :as protocols]
   [io.github.humbleui.ui :as ui]
   [town.lilac.humble.progress :as progress]
   [town.lilac.humble.text-field :as tf])
  (:import
   [java.lang AutoCloseable]
   [io.github.humbleui.skija Canvas]
   [io.github.humbleui.types IPoint IRect]))


(defn with-theme
  [child]
  (ui/dynamic
   ctx
   [scale (:scale ctx)]
   (ui/default-theme
    {:hui.button/bg-inactive (paint/fill 0xFFBBBBBB)
     :hui.progress/track-height (* 10 scale)
     :hui.progress/fill-track-active (paint/fill 0xFF0080FF)
     :hui.progress/fill-track-inactive (paint/fill 0xFFD9D9D9)
     :hui.text-field/fill-bg-disabled (paint/fill 0xFFE0E0E0)}
    child)))


(defn disabled
  [disabled? child]
  (ui/with-context {:hui/disabled? disabled?} child))


(defn invalid
  [error? child]
  (ui/with-context {:hui/error? error?} child))


(defn button
  "Copied from HumbleUI, with disabled state."
  ([on-click child]
   (button on-click nil child))
  ([on-click opts child]
   (ui/dynamic
    ctx
    [{:hui.button/keys [bg bg-active bg-inactive bg-hovered border-radius padding-left padding-top padding-right padding-bottom]
      :hui/keys [disabled?]} ctx]
    (ui/clickable
     {:on-click (when on-click
                  (fn [_] (and (not disabled?) (on-click))))}
     (ui/clip-rrect
      border-radius
      (ui/dynamic
       ctx
       [{:hui/keys [hovered? active? disabled?]} ctx]
       (ui/rect
        (cond
          disabled? bg-inactive
          active?  bg-active
          hovered? bg-hovered
          :else    bg)
        (ui/padding
         padding-left padding-top padding-right padding-bottom
         (ui/center
          (ui/with-context
            {:hui/active? false
             :hui/hovered? false}
            child))))))))))


(def ^{:arglists '([opts])} progress progress/progress)

(def ^{:arglists '([*state] [opts *state])} text-field
  tf/text-field)

(core/deftype+ AbsoluteRect [opts child ^:mut my-rect]
  protocols/IComponent
  (-measure
   [_ ctx cs]
   (core/measure child ctx cs))

  (-draw
   [_ ctx ^IRect rect ^Canvas canvas]
   (let [rect' (assoc rect
                      :x (:x opts)
                      :y (:y opts)
                      :right (:right opts)
                      :bottom (:bottom opts))]
     (set! my-rect rect')
     (core/draw-child
      child ctx
      rect'
      canvas)))

  (-event
   [_ ctx event]
   (core/event-child child ctx event))

  (-iterate
   [this ctx cb]
   (or
    (cb this)
    (protocols/-iterate child ctx cb))))


(defn absolute-rect
  [{:keys [x y] :as opts} child]
  (->AbsoluteRect opts child nil))


(core/deftype+ RelativeRect [relative child opts ^:mut rel-rect ^:mut child-rect]
  protocols/IComponent
  (-measure [_ ctx cs]
    (core/measure child ctx cs))

  (-draw [_ ctx ^IRect rect ^Canvas canvas]
    (let [{:keys [left up anchor shackle]
           :or {left 0 up 0
                anchor :top-left shackle :top-right}} opts
          child-size (core/measure child ctx (IPoint. (:width rect) (:height rect)))
          child-rect' (IRect/makeXYWH (:x rect) (:y rect) (:width child-size) (:height child-size))
          rel-cs (core/measure relative ctx (IPoint. 0 0))
          rel-cs-width (:width rel-cs) rel-cs-height (:height rel-cs)
          rel-rect' (condp = [anchor shackle]
                      [:top-left :top-left]         (IRect/makeXYWH (- (:x child-rect') left) (- (:y child-rect') up) rel-cs-width rel-cs-height)
                      [:top-right :top-left]        (IRect/makeXYWH (- (:x child-rect') rel-cs-width left) (- (:y child-rect') up) rel-cs-width rel-cs-height)
                      [:bottom-right :top-left]     (IRect/makeXYWH (- (:x child-rect') rel-cs-width left) (- (:y child-rect') rel-cs-height up) rel-cs-width rel-cs-height)
                      [:bottom-left :top-left]      (IRect/makeXYWH (- (:x child-rect') left) (- (:y child-rect') rel-cs-height up) rel-cs-width rel-cs-height)
                      [:top-left :top-right]        (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') left)) (- (:y child-rect') up) rel-cs-width rel-cs-height)
                      [:top-right :top-right]       (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') rel-cs-width left)) (- (:y child-rect') up) rel-cs-width rel-cs-height)
                      [:bottom-left :top-right]     (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') left)) (- (:y child-rect') rel-cs-height up) rel-cs-width rel-cs-height)
                      [:bottom-right :top-right]    (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') rel-cs-width left)) (- (:y child-rect') rel-cs-height up) rel-cs-width rel-cs-height)
                      [:top-left :bottom-right]     (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') left)) (+ (:y child-rect') (- (:height child-rect') up)) rel-cs-width rel-cs-height)
                      [:top-right :bottom-right]    (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') rel-cs-width left)) (+ (:y child-rect') (- (:height child-rect') up)) rel-cs-width rel-cs-height)
                      [:bottom-right :bottom-right] (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') rel-cs-width left)) (+ (:y child-rect') (- (:height child-rect') rel-cs-height up)) rel-cs-width rel-cs-height)
                      [:bottom-left :bottom-right]  (IRect/makeXYWH (+ (:x child-rect') (- (:width child-rect') left)) (+ (:y child-rect') (- (:height child-rect') rel-cs-height up)) rel-cs-width rel-cs-height)
                      [:top-left :bottom-left]      (IRect/makeXYWH (- (:x child-rect') left) (+ (:y child-rect') (- (:height child-rect') up)) rel-cs-width rel-cs-height)
                      [:top-right :bottom-left]     (IRect/makeXYWH (- (:x child-rect') rel-cs-width left) (+ (:y child-rect') (- (:height child-rect') up)) rel-cs-width rel-cs-height)
                      [:bottom-left :bottom-left]   (IRect/makeXYWH (- (:x child-rect') left) (+ (:y child-rect') (- (:height child-rect') rel-cs-height up)) rel-cs-width rel-cs-height)
                      [:bottom-right :bottom-left]  (IRect/makeXYWH (- (:x child-rect') rel-cs-width left) (+ (:y child-rect') (- (:height child-rect') rel-cs-height up)) rel-cs-width rel-cs-height))]
      (set! child-rect child-rect')
      (set! rel-rect rel-rect')
      (core/draw-child child ctx child-rect canvas)
      (core/draw-child relative ctx rel-rect canvas)))

  (-event
   [_ ctx event]
   (core/eager-or
    (core/event-child relative ctx event)
    (core/event-child child ctx event)))

  (-iterate [this ctx cb]
    (or
      (cb this)
      (protocols/-iterate child ctx cb)))

  AutoCloseable
  (close [_]
    (core/child-close child)))

(defn relative-rect
  ([relative child] (relative-rect {} relative child))
  ([opts relative child]
   (->RelativeRect relative child opts nil nil)))


(defn fragment
  [children]
  (apply ui/stack children))
