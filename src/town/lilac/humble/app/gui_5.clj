(ns town.lilac.humble.app.gui-5
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.paint :as paint]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]))


(defn crud
  [{:keys [*db *name *surname on-create]}]
  (ui/default-theme
   {}
   (ui/focus-controller
    (ui/center
     (ui/column
      (ui/row
       (ui/valign 0.5 (ui/label "Filter prefix:"))
       (ui/gap 10 10)
       (ui/width
        120
        (ui/text-field (atom {:text ""}))))
      (ui/gap 20 20)
      (ui/row
       (ui/column
        (ui/width
         200
         (ui/height
          100
          (ui/vscrollbar
           (ui/vscroll
            (ui/dynamic
             _ctx
             [db @*db]
             (ui/column
              (for [{:keys [name surname]} db
                    :let [label (ui/padding
                                 8
                                 (ui/label (str surname ", " name))) ]]
                (ui/hoverable
                 (ui/dynamic
                  ctx
                  [hovered? (:hui/hovered? ctx)]
                  (if hovered?
                    (ui/rect (paint/fill 0xFFCFE8FC) label)
                    label)))))))))))
       (ui/gap 5 5)
       (ui/column
        (ui/row
         [:stretch 1 (ui/valign 0.5 (ui/label "Name:"))]
         (ui/width 100 (ui/text-field *name)))
        (ui/gap 10 10)
        (ui/row
         [:stretch 1 (ui/valign 0.5 (ui/label "Surname:"))]
         (ui/gap 10 10)
         (ui/width 100 (ui/text-field *surname)))))
      (ui/gap 20 20)
      (ui/row
       (ui/button on-create (ui/label "Create"))
       (ui/gap 10 10)
       (ui/button nil (ui/label "Update"))
       (ui/gap 10 10)
       (ui/button nil (ui/label "Delete"))))))))


(defn start!
  []
  (let [*db (atom [{:name "Hans" :surname "Emil"}
                   {:name "Max" :surname "Mustermann"}
                   {:name "Roman" :surname "Tisch"}])
        *name (atom {:text ""})
        *surname (atom {:text ""})
        on-create (fn []
                    (swap! *db conj {:name (:text @*name)
                                     :surname (:text @*surname)})
                    (swap! *name assoc :text "")
                    (swap! *surname assoc :text ""))]
    (reset! state/*app (crud {:*db *db
                              :*name *name
                              :*surname *surname
                              :on-create on-create})))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 1000 500)))


(start!)
