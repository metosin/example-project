(ns backend.db
  (:require [next.jdbc.result-set :as rs]
            [camel-snake-kebab.core :as csk]))

;; TODO: Do we want to recommend mapping DB names to kebab-case?

(def opts
  "Default next.jdbc options."
  {:builder-fn rs/as-unqualified-kebab-maps
   :column-fn csk/->snake_case})
