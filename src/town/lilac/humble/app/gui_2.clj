(ns town.lilac.humble.app.gui-2
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.text-field :as tf]))

(defn temp-converter
  [*c *f on-celsius on-fahrenheit]
  (ui/default-theme
   {}
   (ui/focus-controller
    (ui/center
     (ui/row
      (ui/column
       (ui/label "Celsius")
       (ui/gap 5 5)
       (ui/width
        100
        (tf/text-field {:on-change on-celsius} *c)))
      (ui/gap 20 20)
      (ui/column
       (ui/label "Fahrenheit")
       (ui/gap 5 5)
       (ui/width
        100
        (tf/text-field {:on-change on-fahrenheit} *f))))))))


(defn c->f
  [c]
  (+ 32 (* c 9/5)))

(defn f->c
  [f]
  (* (- f 32) 5/9))

(comment
  (c->f 5)
  ;; => 41N
  (f->c 41)
  ;; => 5N
  ,)

(defn safe-parse-float
  " behavior when text field is empty is undefined -- let's return 0.0 "
  [s]
  (if (or (nil? s)
          (= s ""))
    0.0
    (Float/parseFloat s)))

(comment
  (safe-parse-float nil)   ;; => 0.0
  (safe-parse-float "")    ;; => 0.0
  (safe-parse-float "4.0") ;; => 4.0
  0)


(defn start!
  []
  (let [init 5.0
        *c-input (atom {:text (str init)})
        *f-input (atom {:text (str (c->f init))})
        on-celsius (fn on-celsius-change
                     [{:keys [text]}]
                     (swap!
                      *f-input
                      assoc :text
                      (str (c->f (safe-parse-float text)))))
        on-fahrenheit (fn on-fahrenheit-change
                        [{:keys [text]}]
                        (swap!
                         *c-input
                         assoc :text
                         (str (f->c (safe-parse-float text)))))]
    (reset! state/*app (temp-converter
                        *c-input *f-input
                        on-celsius
                        on-fahrenheit)))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 600 200)))


(start!)
