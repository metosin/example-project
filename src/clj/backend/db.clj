(ns backend.db
  (:require [camel-snake-kebab.core :as csk]
            [next.jdbc.result-set :as rs]))

;; TODO: Do we want to recommend mapping DB names to kebab-case?

(def opts
  "Default next.jdbc options."
  {:builder-fn rs/as-unqualified-kebab-maps
   :column-fn csk/->snake_case})
