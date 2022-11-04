(ns town.lilac.humble.ui
  (:require
   [io.github.humbleui.core :as core]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.protocols :as protocols]
   [io.github.humbleui.ui :as ui]
   [town.lilac.humble.progress :as progress]
   [town.lilac.humble.text-field :as tf])
  (:import
   [io.github.humbleui.skija Canvas]
   [io.github.humbleui.types IRect]))


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

(core/deftype+ AbsolutePosition [opts child ^:mut my-rect]
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


(defn absolute
  [{:keys [x y] :as opts} child]
  (->AbsolutePosition opts child nil))


(defn stack
  [children]
  (apply ui/stack children))
