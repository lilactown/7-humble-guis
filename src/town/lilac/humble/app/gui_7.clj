(ns town.lilac.humble.app.gui-7
  (:require
   [clojure.string :as string]
   [io.github.humbleui.app :as app]
   [io.github.humbleui.core :as core]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.ui :as ui2]))

(defn parse-formula:add
  [s]
  (when (string/starts-with? s "=")
    (->> (string/split (subs s 1) #"\+")
         (map #(or (->> (re-matches #"([A-Z])(\d+)" %)
                        (rest)
                        (seq))
                   %))
         (seq))))


(comment
  (parse-formula:add "=A1+B2")

  (parse-formula:add "=A1+B2+C3")
  (parse-formula:add "=A1+123")
  )


(defn parse-formula:number
  [s]
  (try (Float/parseFloat s)
       (catch Throwable _ nil)))


(defmacro cond-let
  [& clauses]
  (when-some [[test expr & rest] clauses]
    (condp = test
      :let  `(if-let ~@expr (cond-let ~@rest))
      `(if ~test ~expr (cond-let ~@rest)))))


(defn calc-formula
  [state i j]
  (let [v (get state [i j])]
    (try
      (cond-let
       ;; add
       :let ([additions (parse-formula:add v)]
             (apply + (map (fn [x]
                             (if (coll? x)
                               (calc-formula state (first x) (second x))
                               (Float/parseFloat x)))
                           additions)))
       :let ([n (parse-formula:number v)]
             n)
       :else "")
      (catch Throwable _
        "#ERR"))))


(calc-formula {["A" "2"] "10" ["B" "2"] "2" ["C" "2"] "=A2+B2"} "C" "2")


(calc-formula {["A" "2"] "10" ["B" "2"] "2" ["C" "2"] "4" ["D" "2"] "=A2+B2+C2"} "D" "2")


(calc-formula {["A" "2"] "10" ["B" "2"] "2" ["C" "2"] "=A2+B2" ["D" "2"] "=C2+4"} "D" "2")


(defn app
  [*state on-cell-change]
  (ui/default-theme
   {:hui.text-field/border-radius 0
    :hui.text-field/fill-bg-inactive (paint/fill 0xFFFFFFFF)}
   (ui/focus-controller
    (ui/vscrollbar
     (ui/vscroll
      (ui2/hscroll
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
               (ui/dynamic
                _ctx [init (calc-formula @*state i j)]
                (ui2/text-field {:on-change #(on-cell-change i j %)} (atom {})))))))))))))))


(defn start!
  []
  (let [*state (atom {})
        on-cell-change (fn [i j v]
                         (swap! *state assoc [i j] v))]
    (reset! state/*app (app *state on-cell-change)))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 1000 700)))


(start!)
