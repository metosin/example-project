(ns common.schema
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [malli.util :as mu]
            [malli.experimental.time]))

(def todo
  [:map
   [:id :int]
   [:text :string]
   [:status [:enum :resolved :unresolved]]
   [:created-at :time/instant]])

(def new-todo
  (-> todo
      (mu/select-keys [:text])))

(def update-todo
  (-> todo
      (mu/select-keys [:text :status])
      (mu/optional-keys)))
