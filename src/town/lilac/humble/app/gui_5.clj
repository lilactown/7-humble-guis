(ns town.lilac.humble.app.gui-5
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]))


(defn crud
  [*count]
  (ui/default-theme
   {}
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
           (ui/column
            (for [i (range 20)]
              (ui/label (str i)))))))))
      (ui/gap 5 5)
      (ui/column
       (ui/row
        [:stretch 1 (ui/valign 0.5 (ui/label "Name:"))]
        (ui/width 100 (ui/text-field (atom {:text ""}))))
       (ui/gap 10 10)
       (ui/row
        [:stretch 1 (ui/valign 0.5 (ui/label "Surname:"))]
        (ui/gap 10 10)
        (ui/width 100 (ui/text-field (atom {:text ""}))))))
     (ui/gap 20 20)
     (ui/row
      (ui/button nil (ui/label "Create"))
      (ui/gap 10 10)
      (ui/button nil (ui/label "Update"))
      (ui/gap 10 10)
      (ui/button nil (ui/label "Delete")))))))


(defn start!
  []
  (reset! state/*app (crud (atom {})))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 1000 500)))


(start!)
