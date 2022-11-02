(ns town.lilac.humble.app.gui-3
  (:require
   [clojure.string :as string]
   [io.github.humbleui.app :as app]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.text-field :as tf]))


(defn disabled
  [disabled? child]
  (ui/with-context {:hui/disabled? disabled?} child))


(defn invalid
  [error? child]
  (ui/with-context {:hui/error? error?} child))


(defn button
  ([on-click child]
   (button on-click nil child))
  ([on-click opts child]
   (ui/dynamic
    ctx
    [{:hui.button/keys [bg bg-active bg-inactive bg-hovered border-radius padding-left padding-top padding-right padding-bottom]} ctx]
    (ui/clickable
     {:on-click (when on-click
                  (fn [_] (on-click)))}
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


(defn flight-booker
  [{:keys [*round-trip?
           *start-input
           *return-input
           on-start
           on-return
           on-book]}]
  (ui/default-theme
   {}
   (ui/dynamic
    ctx
    [{:keys [scale]} ctx
     start-disabled? (:disabled? @*start-input)
     start-error? (:error? @*start-input)
     return-disabled? (:disabled? @*return-input)
     return-error? (:error? @*return-input)
     invalid? (or (:error? @*start-input)
                  (string/blank? (:text @*start-input))
                  (and @*round-trip?
                       (:error? @*return-input))
                  (and @*round-trip?
                       (string/blank? (:text @*return-input))))]
    (ui/with-context
      {:hui.text-field/border-error (paint/stroke 0xFFFF0000 (* 1 scale))
       :hui.button/bg-inactive (paint/fill 0xFFBBBBBB)}
      (ui/focus-controller
       (ui/center
        (ui/column
         (ui/row
          (ui/toggle *round-trip?)
          (ui/gap 20 0)
          (ui/center
           (ui/label "Round trip?")))
         (ui/gap 20 20)
         (disabled
          start-disabled?
          (invalid
           start-error?
           (ui/column
            (ui/label "Start")
            (ui/gap 5 5)
            (ui/width
             200
             (tf/text-field {:on-change on-start} *start-input)))))
         (ui/gap 20 20)
         (disabled
          return-disabled?
          (ui/column
           (ui/label "Return")
           (ui/gap 5 5)
           (ui/width
            200
            (tf/text-field {:on-change on-return} *return-input))))
         (ui/gap 20 20)
         (disabled
          invalid?
          (button on-book (ui/label "Book"))))))))))


(defn parse-date
  [text]
  (re-matches #"(\d\d)\.(\d\d)\.(\d\d\d\d)" text))

(comment
  (parse-date "10.22.2022"))


(defn start!
  []
  (let [*round-trip? (atom false)
        *start-input (atom {:text ""
                            :disabled? false})
        *return-input (atom {:text ""
                             :disabled? true})
        on-toggle (fn on-return-toggle
                    [return?]
                    (swap! *return-input assoc :disabled? (not return?)))
        on-start (fn on-start-change
                   [{:keys [text]}]
                   (if (parse-date text)
                     (swap! *start-input assoc :error? false)
                     (swap! *start-input assoc :error? true)))
        on-return (fn on-return-change
                    [{:keys [text]}]
                    (if (parse-date text)
                     (swap! *return-input assoc :error? false)
                     (swap! *return-input assoc :error? true)))
        on-book (fn on-book
                  [])]
    (add-watch *round-trip? :toggle (fn [_ _ _ round-trip?] (on-toggle round-trip?)))
    (reset! state/*app (flight-booker
                        {:*round-trip? *round-trip?
                         :*start-input *start-input
                         :*return-input *return-input
                         :on-start on-start
                         :on-return on-return
                         :on-book on-book})))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 600 600)))


(start!)
