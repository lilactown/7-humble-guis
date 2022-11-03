(ns town.lilac.humble.app.gui-3
  (:require
   [clojure.string :as string]
   [io.github.humbleui.app :as app]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.text-field :as tf]))


(defn parse-date
  [text]
  (re-matches #"(\d\d)\.(\d\d)\.(\d\d\d\d)" text))

(comment
  (parse-date "10.22.2022")
  ;; => ["10.22.2022" "10" "22" "2022"]
  )

(defn before?
  [start-text return-text]
  (let [[mm0 dd0 yyyy0] (->> (parse-date start-text)
                             (rest)
                             (map #(Integer/parseInt %)))
        [mm1 dd1 yyyy1] (->> (parse-date return-text)
                             (rest)
                             (map #(Integer/parseInt %)))]
    (when (and mm0 mm1 dd0 dd1 yyyy0 yyyy1)
      (or
       (< yyyy0 yyyy1)
       (and (= yyyy0 yyyy1)
            (< mm0 mm1))
       (and (= yyyy0 yyyy1)
            (= mm0 mm1)
            (<= dd0 dd1))))))

(comment
  (before? "arst" "mnei")
  ;; => nil

  (before? "10.23.2023" "10.11.2022")
  ;; => false

  (before? "10.23.2023" "10.24.2023")
  ;; => true

  (before? "10.23.2022" "10.12.2023")
  ;; => true
  )


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
  [{:keys [*booked
           *round-trip?
           *start-input
           *return-input
           on-start
           on-return
           on-book]}]
  (ui/default-theme
   {:hui.button/bg-inactive (paint/fill 0xFFBBBBBB)
    :hui.text-field/fill-bg-disabled (paint/fill 0xFFE0E0E0)}
   (let [start (ui/column
                (ui/label "Start")
                (ui/gap 5 5)
                (ui/width
                 200
                 (tf/text-field {:on-change on-start} *start-input)))
         return (ui/column
                 (ui/label "Return")
                 (ui/gap 5 5)
                 (ui/width
                  200
                  (tf/text-field {:on-change on-return} *return-input))) ]
     (ui/dynamic
      ctx
      [{:keys [scale]} ctx
       booked @*booked
       start-disabled? (:disabled? @*start-input)
       start-error? (:error? @*start-input)
       return-disabled? (:disabled? @*return-input)
       return-error? (:error? @*return-input)
       invalid? (or (:error? @*start-input)
                    (string/blank? (:text @*start-input))
                    (and @*round-trip?
                         (:error? @*return-input))
                    (and @*round-trip?
                         (string/blank? (:text @*return-input)))
                    (and @*round-trip?
                         (not (before? (:text @*start-input)
                                       (:text @*return-input)))))]
      (ui/with-context
        {:hui.text-field/border-error (paint/stroke 0xFFFF0000 (* 1 scale))}
        (ui/focus-controller
         (ui/center
          (ui/column
           (ui/row
            (ui/toggle *round-trip?)
            (ui/gap 20 0)
            (ui/center
             (ui/label "Round trip?")))
           (ui/gap 20 20)
           (disabled start-disabled? (invalid start-error? start))
           (ui/gap 20 20)
           (disabled return-disabled? (invalid return-error? return))
           (ui/gap 20 20)
           (disabled invalid? (button on-book (ui/label "Book")))
           (ui/gap 10 10)
           (ui/width
            200
            (ui/height
             30
             (ui/column
              (for [line (string/split-lines (or booked ""))]
                (ui/column
                 (ui/gap 5 5)
                 (ui/label line))))))))))))))


(defn start!
  []
  (let [*round-trip? (atom false)
        *start-input (atom {:text "10.23.2023"
                            :disabled? false})
        *return-input (atom {:text "10.23.2023"
                             :disabled? true})
        *booked (atom nil)
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
                  []
                  (reset! *booked (str "You have booked a "
                                       (if @*round-trip? "round-trip" "one-way")
                                       "\nflight on " (:text @*start-input)
                                       (if @*round-trip?
                                         (str ", returning\non " (:text @*return-input))
                                         ""))))]
    (add-watch *round-trip? :toggle (fn [_ _ _ round-trip?] (on-toggle round-trip?)))
    (reset! state/*app (flight-booker
                        {:*booked *booked
                         :*round-trip? *round-trip?
                         :*start-input *start-input
                         :*return-input *return-input
                         :on-start on-start
                         :on-return on-return
                         :on-book on-book})))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 600 600)))


(start!)
