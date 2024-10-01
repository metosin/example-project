(ns backend.api.todo
  (:require [ring.util.http-response :as resp]
            [next.jdbc.sql :as sql]
            [backend.db :as db]
            [common.schema :as schema]
            [malli.core :as m]
            [malli.transform :as mt]))

(defn get-todo [req]
  (let [res (sql/find-by-keys (:db req) :todo :all db/opts)]
    (resp/ok res)))
