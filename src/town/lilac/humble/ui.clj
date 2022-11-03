(ns town.lilac.humble.ui
  (:require
   [io.github.humbleui.ui :as ui]
   [town.lilac.humble.text-field :as tf]))


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


(def ^{:arglists '([*state] [opts *state])} text-field
  tf/text-field)
