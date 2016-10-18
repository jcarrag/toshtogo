(ns toshtogo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :refer [pprint]]))

(enable-console-print!)

(println "Hello world!")

(def app-state (atom {:page [1 2 3]
                      :jobs [{:job 1} {:job 2} {:job 3}]}))

(defmulti read (fn [env key params] key))

(defmethod read :jobs
  [{:keys [state] :as env} key {:keys [start end]}]
  {:value (subvec (:jobs @state) start end)}) 

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defn mutate
  [{:keys [state] :as env} key params]
  (if (= 'add-job key)
    {:value {:keys [:jobs]}
     :action #(swap! state update-in [:jobs] conj {:job 9000})}
    {:value :not-found}))

(defui JobBody
  static om/IQueryParams
  (params [this]
          {:start 0 :end 2})
  static om/IQuery
  (query [this]
         '[:page (:jobs {:start ?start :end ?end})])
  Object
  (render [this]
          (let [{:keys [jobs page]} (om/props this)]
            (dom/div nil
                     (dom/div nil page)
                     (dom/button
                       #js {:onClick (fn [e] (om/transact! this '[(add-job)]))}
                       "Add a job!")
                     (dom/div nil
                              (with-out-str
                                (pprint jobs))))))) 

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (om/parser {:read read
                         :mutate mutate})}))

(om/add-root!
  reconciler
  JobBody
  (gdom/getElement "app"))
