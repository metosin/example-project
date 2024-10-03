(ns backend.test-utils
  (:require [backend.main :as main]
            [backend.migrations :as migrations]
            [hikari-cp.core :as hikari-cp]
            [integrant.core :as ig]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn test-config []
  (merge-with merge
              (main/system-config)
              {:adapter/jetty {:port 3337}
               :database/pool {:database-name "example_test"}}))

(defn ensure-template-db!
  []
  (let [template-db "example_template"]
    (with-open [db (hikari-cp/make-datasource (:database/pool (main/system-config)))]
      (let [template-db-exists (:?column? (first (sql/query db ["select 1 from pg_database where datname=?" template-db])))]
        (when-not template-db-exists
          (jdbc/execute! db ["create database example_template"]))
        (with-open [tdb (hikari-cp/make-datasource (assoc (:database/pool (main/system-config))
                                                          :database-name template-db))]
          (migratus/migrate (migrations/migratus-config {:database/pool tdb})))))))

(comment
  ;; You should run this before running tests from REPL after adding a migration
  (ensure-template-db))

(defn ensure-test-db!
  []
  (let [test-db (:database-name (:database/pool (test-config)))
        template-db "example_template"]
    (with-open [db (hikari-cp/make-datasource (:database/pool (main/system-config)))]
      (jdbc/execute! db [(str "drop database if exists " test-db)])
      (jdbc/execute! db [(str "create database " test-db " template " template-db)]))))

(def ^:dynamic *system* nil)

(defn system []
  *system*)

(defn db []
  (:database/pool (system)))

(defn with-test-system [f]
  (ensure-test-db!)
  (let [system (ig/init (test-config))]
    (try
      (binding [*system* system]
        (f))
      (finally
        (ig/halt! system)))))
