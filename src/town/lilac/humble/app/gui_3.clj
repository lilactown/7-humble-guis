(ns town.lilac.humble.app.gui-3
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.text-field :as tf]))

(defn flight-booker
  [*return *start-input *return-input on-start on-return on-book]
  (ui/default-theme
   {}
   (ui/focus-controller
    (ui/center
     (ui/column
      (ui/row
       (ui/toggle *return)
       (ui/gap 20 0)
       (ui/center
        (ui/label "Return?")))
      (ui/gap 20 20)
      (ui/column
       (ui/label "Start")
       (ui/gap 5 5)
       (ui/width
        200
        (tf/text-field {:on-change on-start} *start-input)))
      (ui/gap 20 20)
      (ui/column
       (ui/label "Return")
       (ui/gap 5 5)
       (ui/width
        200
        (tf/text-field {:on-change on-return} *return-input)))
      (ui/gap 20 20)
      (ui/button on-book (ui/label "Book")))))))


(defn start!
  []
  (let [*return (atom false)
        *start-input (atom {:text ""})
        *return-input (atom {:text ""
                             :disabled? true})
        on-toggle (fn on-return-toggle
                    [return?]
                    (swap! *return-input assoc :disabled? (not return?)))
        on-start (fn on-start-change
                     [{:keys [text]}]
                     )
        on-return (fn on-return-change
                    [{:keys [text]}]
                    )
        on-book (fn on-book
                  [])]
    (add-watch *return :toggle (fn [_ _ _ return?] (on-toggle return?)))
    (reset! state/*app (flight-booker
                        *return *start-input *return-input
                        on-start
                        on-return
                        on-book)))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 600 600)))


(start!)