(ns toshtogo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :refer [pprint]]))

(enable-console-print!)

(def init-data
  {:page/one [{:job-id 1 :outcome true}
              {:job-id 2 :outcome false}
              {:job-id 3 :outcome true}]
   :page/two [{:job-id 3 :outcome false}
              {:job-id 4 :outcome true}
              {:job-id 5 :outcome true}]})

(defmulti read om/dispatch)

(defn get-jobs [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :page/one
  [{:keys [state ast] :as env} key params]
  (pprint @state)
  {:value (get-jobs state key)
   :api ast})

(defmethod read :page/two
  [{:keys [state ast] :as env} key params]
  {:value (get-jobs state key)
   :api ast})

(defmulti mutate om/dispatch)

(defmethod mutate 'outcome/toggle
  [{:keys [state]} _ {:keys [job-id]}]
  {:action
   (fn []
     (swap! state update-in [:jobs/by-job-id job-id :outcome] not))})

(defui Job
  static om/Ident
  (ident [this {:keys [job-id]}]
         [:jobs/by-job-id job-id])
  static om/IQuery
  (query [this]
         '[:job-id :outcome])
  Object
  (render [this]
          (let [{:keys [job-id outcome] :as props} (om/props this)]
            (dom/div nil
                     (dom/div #js {:onClick #(om/transact! this `[(outcome/toggle ~props)])}
                              (str "job-id: " job-id ", outcome: " outcome)))))) 

(def job
  (om/factory Job {:keyfn :job-id}))

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

(defn remotes-loop [c]
  (go-loop [[query cb] (<! c)]
           (let [response (str "Reponse to: " query)] 
             (cb {:hardcoded-response-key response}))
           (recur (<! c))))

(defmulti remote-query)

(defn send-to-chan [c]
  (fn [remote-query cb]
    (println :remote-query remote-query)
    (let [ast (om/query->ast remote-query)]
      (put! c [remote-query cb]))))

(def remotes-chan (chan))

(def reconciler
  (om/reconciler
    {:state init-data
     :parser (om/parser {:read read
                         :mutate mutate})
     :send (send-to-chan remotes-chan)
     :remotes [:api]}))

(remotes-loop remotes-chan)

(om/add-root!
  reconciler
  RootView
  (gdom/getElement "app"))
