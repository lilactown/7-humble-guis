(ns town.lilac.humble.progress
  (:require
   [io.github.humbleui.canvas :as canvas]
   [io.github.humbleui.core :as core]
   [io.github.humbleui.protocols :as protocols])
  (:import
   [io.github.humbleui.types IPoint IRect Point Rect RRect]))

;; this code is based off of the humble slider

(core/deftype+ ProgressTrack [fill-key]
  protocols/IComponent
  (-measure [_ ctx cs] cs)

  (-draw
   [this ctx ^IRect rect ^Canvas canvas]
   (let [{:hui.progress/keys [track-height]} ctx
         half-track-height (/ track-height 2)
         x      (- (:x rect) half-track-height)
         y      (+ (:y rect) (/ (:height rect) 2))
         w      (+ (:width rect) track-height)
         r      half-track-height
         rect   (core/rrect-xywh x y w track-height r)]
     (canvas/draw-rect canvas rect (ctx fill-key))))

  (-event [this ctx event])

  (-iterate [this ctx cb]))


(core/deftype+ Progress [opts track-active track-inactive ^:mut my-rect]
  protocols/IComponent
  (-measure
   [_ ctx cs]
   (assoc cs :height (:hui.progress/track-height ctx)))

  (-draw
   [this ctx ^IRect rect ^Canvas canvas]
   (set! my-rect rect)
   (let [{:keys [value min max step]} opts
         {:hui.progress/keys [track-height]} ctx
         {x :x, y :y, w :width} my-rect
         range (- max min)
         value (if (< value max) value max)
         ratio (/ (- value min) range)
         active-x (+ x (* ratio w))]
     (core/draw
      track-inactive ctx
      (core/irect-ltrb active-x y (+ x w) y)
      canvas)
     (core/draw
      track-active ctx
      (core/irect-ltrb x y active-x y)
      canvas)))

  (-event [this ctx event])

  (-iterate [this ctx cb]))


(defn progress
  [opts ]
  (let [{:keys [track-active track-inactive]
         :or {track-active (->ProgressTrack :hui.progress/fill-track-active)
              track-inactive (->ProgressTrack :hui.progress/fill-track-inactive)}} opts]
    (->Progress
     (core/merge-some
      {:value     (:min opts 0)
       :min       0
       :max       100
       :step      1
       :delta-x   0}
      opts)
     track-active track-inactive nil)))
