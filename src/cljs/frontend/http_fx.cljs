(ns frontend.http-fx
  (:require ["@js-joda/core" :refer [Instant]]
            [cognitect.transit :as transit]
            [re-frame.core :as rf]
            [superstructor.re-frame.fetch-fx :as fetch-fx]))

;; - Re-frame-http-fx uses cljs-ajax which is wrapper over XmlHttpRequest,
;;    while it works I much prefer using Fetch API now.
;; - Re-frame-http-fx-alpha is also an good idea, but is is very alpha and not updated in years.
;; - Re-frame-fetch-fx looks somewhat OK -> use that as basis, extend it with
;;   eransit and reusable events to store request status.

(def r (transit/reader :json
                       {:handlers {"Instant" (transit/read-handler #(.parse Instant %))}}))

(defn read-transit [s]
  (transit/read r s))

;; TODO: Integrated :path handling?

(rf/reg-fx ::fetch
  (fn [effect]
    (doseq [effect (fetch-fx/->seq effect)]
      (fetch-fx/fetch (merge-with merge
                                  {:envelope? false
                                   :response-content-types {#"application/json" :json
                                                            #"application/transit\+json" {:reader-kw :text
                                                                                          :reader-fn read-transit}}
                                   :headers {"Accept" "transit/application+json"}}
                                  effect)))))

;; Reusable events to store http request status to app-db
;; I suggest to include resource ids ("identity") in the path,
;; so e.g. user 1 and user 2 would be stored in different paths and have
;; their own status.
;; This is kind of inspired by TSQ and Re-frame-http-fx-alpha.
;; TODO: Consider ways to clear up old data from app-db automatically?
;; TODO: Status enum could be better split into a few status flags, see TSQ.

(rf/reg-event-db :http/init
  (fn [db [_ path]]
    (update db :http update-in path update :status (fn [x]
                                                     (case x
                                                       :ready :loading
                                                       :initial-loading)))))

(rf/reg-event-db :http/success
  (fn [db [_ path resp]]
    (update db :http update-in path (fn [x]
                                      (-> x
                                          (assoc :status :ready)
                                          (assoc :resp resp))))))

(rf/reg-event-db :http/failure
  (fn [db [_ path resp]]
    (update db :http update-in path (fn [x]
                                      (-> x
                                          (assoc :status :error)
                                          (assoc :error resp))))))

(rf/reg-sub :http/body
  (fn [db [_ path]]
    (:body (:resp (get-in (:http db) path)))))
