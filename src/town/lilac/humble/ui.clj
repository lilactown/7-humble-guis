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
   [io.github.humbleui.skija Canvas Paint]
   [io.github.humbleui.types IPoint IRect RRect]))


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


(core/deftype+ HScroll [child ^:mut offset ^:mut ^IRect self-rect ^:mut child-size]
  protocols/IComponent
  (-measure [_ ctx cs]
    (let [child-cs (assoc cs :width Integer/MAX_VALUE)]
      (set! child-size (protocols/-measure child ctx child-cs))
      (IPoint. (:width cs) (:height child-size))))

  (-draw [_ ctx ^IRect rect ^Canvas canvas]
    (when (nil? child-size)
      (set! child-size (protocols/-measure child ctx (IPoint. Integer/MAX_VALUE (:height rect)))))
    (set! self-rect rect)
    (set! offset (core/clamp offset (- (:width rect) (:width child-size)) 0))
    (let [layer      (.save canvas)
          child-rect (-> rect
                       (update :x + offset)
                       (assoc :width Integer/MAX_VALUE))]
      (try
        (.clipRect canvas (.toRect rect))
        (core/draw child ctx child-rect canvas)
        (finally
          (.restoreToCount canvas layer)))))

  (-event [_ ctx event]
    (cond
      (= :mouse-scroll (:event event))
      (when (.contains self-rect (IPoint. (:x event) (:y event)))
        (or
          (core/event-child child ctx event)
          (let [offset' (-> offset
                          (+ (:delta-x event))
                          (core/clamp (- (:width self-rect) (:width child-size)) 0))]
            (when (not= offset offset')
              (set! offset offset')
              true))))

      (= :mouse-button (:event event))
      (when (.contains self-rect (IPoint. (:x event) (:y event)))
        (core/event-child child ctx event))

      :else
      (core/event-child child ctx event)))

  (-iterate [this ctx cb]
    (or
      (cb this)
      (protocols/-iterate child ctx cb)))

  AutoCloseable
  (close [_]
    (core/child-close child)))

(defn hscroll [child]
  (->HScroll child 0 nil nil))


(core/deftype+ HScrollbar [child ^Paint fill-track ^Paint fill-thumb ^:mut child-rect]
  protocols/IComponent
  (-measure [_ ctx cs]
    (core/measure child ctx cs))

  (-draw [_ ctx rect ^Canvas canvas]
    (set! child-rect rect)
    (core/draw-child child ctx child-rect canvas)
    (when (> (:width (:child-size child)) (:width child-rect))
      (let [{:keys [scale]} ctx
            content-x (- (:offset child))
            content-w (:width (:child-size child))
            scroll-x  (:x child-rect)
            scroll-w  (:width child-rect)

            padding (* 4 scale)
            track-h (* 4 scale)
            ;; track-x (+ (:x rect) (:width child-rect) (- track-w) (- padding))
            ;; track-y (+ scroll-y padding)
            ;; track-h (- scroll-h (* 2 padding))
            track-x (+ scroll-x padding)
            track-y (+ (:y rect) (:height child-rect) (- track-h) (- padding))
            track-w (- scroll-w (* 2 padding))
            track   (RRect/makeXYWH track-x track-y track-w track-h (* 2 scale))

            thumb-w       (* 4 scale)
            min-thumb-h   (* 16 scale)
            thumb-x-ratio (/ content-x content-w)
            thumb-x       (-> (* track-h thumb-x-ratio) (core/clamp 0 (- track-h min-thumb-h)) (+ track-y))
            thumb-r-ratio (/ (+ content-x scroll-w) content-w)
            thumb-r       (-> (* track-w thumb-x-ratio) (core/clamp min-thumb-h track-h) (+ track-y))
            thumb         (RRect/makeLTRB thumb-x track-y thumb-r (+ track-x thumb-w) (* 2 scale))]
        (.drawRRect canvas track fill-track)
        #_(.drawRRect canvas thumb fill-thumb))))

  (-event [_ ctx event]
    (core/event-child child ctx event))

  (-iterate [this ctx cb]
    (or
      (cb this)
      (protocols/-iterate child ctx cb)))

  AutoCloseable
  (close [_]
    ;; TODO causes crash
    ; (.close fill-track)
    ; (.close fill-thumb)
    (core/child-close child)))

(defn hscrollbar [child]
  (when-not (instance? HScroll child)
    (throw (ex-info (str "Expected VScroll, got: " (type child)) {:child child})))
  (->HScrollbar child (paint/fill 0xFF000000) (paint/fill 0x60000000) nil))
