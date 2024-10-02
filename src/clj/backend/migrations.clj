(ns backend.migrations
  (:require [backend.main :as main]
            [integrant.core :as ig]
            [migratus.core :as migratus]))

(defn migratus-config
  [system]
  {:store                :database
   :migration-dir        "resources/db/migrations/"
   :init-in-transaction? true
   :migration-table-name "migrations"
   :db                   {:datasource (:database/pool system)}})

(defn run-migration-system [migration-function]
  (let [system (ig/init (main/system-config) [:database/pool])]
    (try
      (migration-function (migratus-config system))
      (finally
        (ig/halt! system)))))

(defn create-migration [name type]
  (let [type-kw (or (keyword type) :sql)]
    (migratus/create (dissoc (migratus-config nil) :db) (or name "unknown") type-kw)))

(defn -main [& args]
  (let [command (first args)]
    (case command
      "init"    (run-migration-system migratus/init)
      "migrate" (run-migration-system migratus/migrate)
      "create"  (let [[name type] (rest args)]
                  (create-migration name type))
      (throw (ex-info (str "Unknown command \"" command "\" Available commands are: init, migrate") {})))))

(comment
  (run-migration-system migratus/migrate))
