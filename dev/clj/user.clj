(ns user
  (:require [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [integrant.repl.state :as state]
            [migratus.core :as migratus]))

;; Do not require project ns in user.ns ns form
;; If you do, errors in project ns would prevent starting up the repl

(integrant.repl/set-prep! (fn []
                            ((requiring-resolve 'backend.main/system-config))))

(defn system [] (or state/system
                    (throw (ex-info "System not running" {}))))

(defn env [] (system))

(defn db [] (:database/pool (env)))

(defn create-migration [name]
  ((requiring-resolve 'backend.migrations/create-migration) name :sql))

(defn migrate []
  (migratus/migrate ((requiring-resolve 'backend.migrations/migratus-config) (system))))
