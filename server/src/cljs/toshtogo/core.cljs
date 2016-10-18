(ns toshtogo.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :refer [pprint]]))

(enable-console-print!)

(def init-data
  {:page/one [{:id 1 :success true}
              {:id 2 :success false}
              {:id 3 :success true}]
   :page/two [{:id 3 :success false}
              {:id 4 :success true}
              {:id 5 :success true}]})

(defmulti read om/dispatch)

(defn get-jobs [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :page/one
  [{:keys [state] :as env} key params]
  {:value (get-jobs state key)})

(defmethod read :page/two
  [{:keys [state] :as env} key params]
  {:value (get-jobs state key)})

(defmulti mutate om/dispatch)

(defmethod mutate 'success/toggle
  [{:keys [state]} _ {:keys [id]}]
  {:action
   (fn []
     (swap! state update-in [:jobs/by-id id :success] not))})

(defui Job
  static om/Ident
  (ident [this {:keys [id]}]
         [:jobs/by-id id])
  static om/IQuery
  (query [this]
         '[:id :success])
  Object
  (render [this]
          (let [{:keys [id success] :as props} (om/props this)]
            (dom/div nil
                     (dom/div #js {:onClick #(om/transact! this `[(success/toggle ~props)])}
                              (str "id: ," id ", success: " success)))))) 

(def job
  (om/factory Job {:keyfn :id}))

(defui PageView
  Object
  (render [this]
          (let [page (om/props this)]
            (apply dom/ul nil
                   (map job page)))))

(def page-view
  (om/factory PageView)) 

(defui RootView
  static om/IQuery
  (query [this]
         (let [subquery (om/get-query Job)]
           `[{:page/one ~subquery} {:page/two ~subquery}]))
  Object
  (render [this]
          (let [{:keys [page/one page/two]} (om/props this)]
            (apply dom/div nil
                   [(dom/h2 nil "Page one")
                    (page-view one)
                    (dom/h2 nil "Page two")
                    (page-view two)]))))

(def reconciler
  (om/reconciler
    {:state init-data
     :parser (om/parser {:read read
                         :mutate mutate})}))

(om/add-root!
  reconciler
  RootView
  (gdom/getElement "app"))
