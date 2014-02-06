(ns toshtogo.client.clients.json-converting-client
  (:require [clj-time.format :as tf]
            [flatland.useful.map :refer [update update-each]]
            [toshtogo.util.core :refer [uuid cause-trace parse-datetime]]
            [toshtogo.client.protocol :refer :all]
            [toshtogo.client.senders.protocol :refer :all]))

(defn convert-job [job]
  (when job
    (-> job
        (update-each [:contract_created :contract_claimed :contract_due :contract_finished :job_created :last_heartbeat] parse-datetime)
        (update-each [:commitment_id :contract_id :job_id :requesting_agent] uuid)
        (update-each [:outcome] keyword))))

(defn convert-heartbeat [heartbeat]
  (update heartbeat :instruction keyword))

(defn json-converting-client
  [decorated]
  (reify
    Client
    (put-job! [this job-id job-req]
      (convert-job (put-job! decorated job-id job-req)))

    (get-job [this job-id]
      (convert-job (get-job decorated job-id)))

    (pause-job! [this job-id]
      (pause-job! decorated job-id))

    (request-work! [this tags]
      (convert-job (request-work! decorated tags)))

    (heartbeat! [this commitment-id]
      (convert-heartbeat (heartbeat! decorated commitment-id)))

    (complete-work! [this commitment-id result]
      (complete-work! decorated commitment-id result))))