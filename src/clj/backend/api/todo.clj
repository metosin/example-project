(ns backend.api.todo
  (:require [backend.db :as db]
            [next.jdbc.sql :as sql]
            [ring.util.http-response :as resp]))

(defn get-todo [req]
  (let [res (sql/find-by-keys (:db req) :todo :all db/opts)]
    (resp/ok res)))

(defn create-todo [req]
  (sql/insert! (:db req) :todo (:body (:parameters req))))

(defn update-todo [req]
  (sql/update! (:db req) :todo (:body (:parameters req)) ["id = ?" (:id (:query (:parameters req)))])
  (resp/ok))
