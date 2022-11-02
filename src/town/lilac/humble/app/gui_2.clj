(ns town.lilac.humble.app.gui-2
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.text-field :as tf]))

(defn temp-converter
  [*c *f]
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
        (ui/key-listener
         {:on-key-down #(prn %)}
         (tf/text-field *c))))
      (ui/gap 20 20)
      (ui/column
       (ui/label "Fahrenheit")
       (ui/gap 5 5)
       (ui/width
        100
        (tf/text-field *f))))))))


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
  )

(defn start!
  []
  (let [init 5
        *c (atom init)
        *c-input (atom {:text (str init)})
        *f-input (atom {:text (str (c->f init))})]
    ;;(add-watch *c-input )
    (reset! state/*app (temp-converter *c-input *f-input)))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 600 200)))


(start!)
