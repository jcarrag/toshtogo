(ns ^:figwheel-always toshtogo.util.history
  (:import goog.History)
  (:require [secretary.core :as secretary ]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  )

(defonce history
         (History.))

(defn navigate [location]
  (println "NAV" location)
  (. history (setToken location "")))

(defonce navigation-listener
         (events/listen history EventType/NAVIGATE #(secretary/dispatch! (.-token %))))

(.setEnabled history true)
