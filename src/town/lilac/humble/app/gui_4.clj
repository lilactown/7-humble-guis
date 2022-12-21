(ns town.lilac.humble.app.gui-4
  (:require
   [io.github.humbleui.app :as app]
   [io.github.humbleui.ui :as ui]
   [io.github.humbleui.window :as window]
   [town.lilac.humble.app.state :as state]
   [town.lilac.humble.ui :as ui2]))

(defn timer
  [*config *timer on-reset]
  (ui/dynamic
   ctx
   [scale (:scale ctx)]
   (ui2/with-theme
     (ui/center
      (ui/width
       350
       (ui/column
        (ui/row
         (ui/label "Elapsed: ")
         ;; progress bar is a little wider than it should be
         (ui/gap 5 5)
         (ui/dynamic
          _ctx
          [elapsed @*timer
           max (:value @*config)]
          (ui2/progress {:value elapsed
                         :max max}))
         (ui/gap 5 5))
        (ui/gap 10 10)
        (ui/dynamic
         _ctx
         [elapsed @*timer]
         (ui/label (str (float (/ elapsed 1000))
                        "s")))
        (ui/gap 10 10)
        (ui/row
         (ui/label "Duration:")
         (ui/slider *config))
        (ui/gap 20 20)
        (ui/button on-reset (ui/label "Reset"))))))))


(defonce *halt! nil)


(defn start!
  []
  (let [*config (atom {:max (* 60 1000 5) ; 5 min
                       :value (* 60 1000) ; 1 min
                       })
        *timer (atom 0)
        *inner-halt! (atom false)
        run-timer! (fn run-timer! []
                     (future
                       (Thread/sleep 100)
                       (when (< @*timer (:value @*config))
                         (swap! *timer + 100))
                       (when-not @*inner-halt!
                         (run-timer!))))
        on-reset #(reset! *timer 0)]
    (add-watch *timer ::redraw (fn [_ _ _ _] (state/redraw!)))
    (reset! state/*app (timer *config *timer on-reset))
    (run-timer!)
    (alter-var-root #'*halt! (constantly *inner-halt!)))
  (state/redraw!)
  (app/doui
   (window/set-content-size @state/*window 800 400)))

(and *halt! (reset! *halt! true))
(start!)
