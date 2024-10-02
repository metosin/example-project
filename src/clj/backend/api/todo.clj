(ns backend.api.todo
  (:require [backend.db :as db]
            [common.schema :as schema]
            [malli.core :as m]
            [malli.transform :as mt]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [ring.util.http-response :as resp]))

(def todo-coercer (m/coercer schema/todo mt/string-transformer))
(def todos-coercer (m/coercer [:sequential schema/todo] mt/string-transformer))

(defn get-todo [db id]
  (let [res (sql/get-by-id db :todo id :id db/opts)
        res (todo-coercer res)]
    res))

(defn get-todos [req]
  (let [res (sql/find-by-keys (:db req) :todo :all db/opts)
        res (todos-coercer res)]
    (resp/ok res)))

(defn create-todo [req]
  ;; Postgres returns the inserted row
  (let [created (sql/insert! (:db req) :todo (:body (:parameters req)) db/opts)
        created (todo-coercer created)]
    (resp/ok created)))

(defn update-todo [req]
  (jdbc/with-transaction [tx (:db req)]
    (sql/update! tx :todo (:body (:parameters req)) ["id = ?" (:id (:path (:parameters req)))] db/opts)
    (get-todo tx (:id (:path (:parameters req))) )))

(defn delete-todo [req]
  (let [x (sql/delete! (:db req) :todo ["id = ?" (:id (:path (:parameters req)))] db/opts)]
    (if (pos? (:next.jdbc/update-count x))
      (resp/ok)
      (resp/not-found))))
