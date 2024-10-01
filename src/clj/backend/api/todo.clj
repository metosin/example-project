(ns backend.api.todo
  (:require [backend.db :as db]
            [next.jdbc.sql :as sql]
            [ring.util.http-response :as resp]))

(defn get-todo [req]
  (let [res (sql/find-by-keys (:db req) :todo :all db/opts)]
    (resp/ok res)))
