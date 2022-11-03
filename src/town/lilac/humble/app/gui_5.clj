(ns town.lilac.humble.app.gui-5
  (:require
   [clojure.string :as string]
   [io.github.humbleui.app :as app]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.ui :as ui2]))


(defn crud
  [{:keys [*db *filter *name *surname on-create on-select on-update on-delete]}]
  (ui2/with-theme
    (ui/focus-controller
     (ui/padding
      15
      (ui/column
       (ui/row
        (ui/valign 0.5 (ui/label "Filter prefix:"))
        (ui/gap 10 10)
        (ui/width
         120
         (ui/text-field *filter)))
       (ui/gap 20 20)
       [:stretch 1
        (ui/row
         [:stretch 1
          (ui/column
           (ui/vscrollbar
            (ui/vscroll
             (ui/dynamic
              _ctx
              [{:keys [entries selected]} @*db
               filter-text (:text @*filter)]
              (ui/column
               (for [[i {:keys [name surname]}] (map-indexed vector entries)
                     :when (string/starts-with? surname filter-text)
                     :let [label (ui/padding
                                  8
                                  (ui/label (str surname ", " name)))]]
                 (ui/hoverable
                  (ui/clickable
                   {:on-click (fn [_] (on-select i))}
                   (ui/dynamic
                    ctx
                    [hovered? (:hui/hovered? ctx)]
                    (cond
                      (= selected i)
                      (ui/rect (paint/fill 0xFFAACCFF) label)

                      hovered?
                      (ui/rect (paint/fill 0xFFCFE8FC) label)

                      :else
                      label))))))))))]
         (ui/gap 5 5)
         (ui/column
          (ui/row
           [:stretch 1 (ui/valign 0.5 (ui/label "Name:"))]
           (ui/width 100 (ui/text-field *name)))
          (ui/gap 10 10)
          (ui/row
           [:stretch 1 (ui/valign 0.5 (ui/label "Surname:"))]
           (ui/gap 10 10)
           (ui/width 100 (ui/text-field *surname)))))]
       (ui/gap 20 20)
       (ui/row
        (ui/button on-create (ui/label "Create"))
        (ui/gap 10 10)
        (ui/dynamic
         _ctx
         [selected? (:selected @*db)]
         (ui2/disabled (not selected?) (ui2/button on-update (ui/label "Update"))))
        (ui/gap 10 10)
        (ui/dynamic
         _ctx
         [selected? (:selected @*db)]
         (ui2/disabled
          (not selected?)
          (ui2/button on-delete (ui/label "Delete"))))))))))


(defn start!
  []
  (let [*db (atom {:entries [{:name "Hans" :surname "Emil"}
                             {:name "Max" :surname "Mustermann"}
                             {:name "Roman" :surname "Tisch"}]
                   :selected nil
                   :filter ""})
        *name (atom {:text ""})
        *surname (atom {:text ""})
        *filter (atom {:text ""})
        on-select (fn [i]
                    (swap! *db assoc :selected i)
                    (swap! *name assoc :text (get-in @*db [:entries i :name]))
                    (swap! *surname assoc :text (get-in @*db [:entries i :surname])))
        on-create (fn []
                    (swap! *db update :entries conj {:name (:text @*name)
                                                     :surname (:text @*surname)})
                    (swap! *db assoc :selected nil)
                    (swap! *name assoc :text "")
                    (swap! *surname assoc :text ""))
        on-update (fn []
                    (let [i (:selected @*db)]
                      (swap! *db assoc-in [:entries i] {:name (:text @*name)
                                                        :surname (:text @*surname)})))
        on-delete (fn []
                    (let [i (:selected @*db)]
                      (swap! *db update :entries
                             #(into (subvec % 0 i)
                                    (subvec % (inc i))))
                      (swap! *db assoc :selected nil)))]
    (reset! state/*app (crud {:*db *db
                              :*filter *filter
                              :*name *name
                              :*surname *surname
                              :on-create on-create
                              :on-select on-select
                              :on-update on-update
                              :on-delete on-delete})))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 1000 500)))


(start!)
