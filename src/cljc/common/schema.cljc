(ns common.schema
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [malli.util :as mu]
            [malli.core :as malli]
            [malli.experimental.time :as malli.time]
            [malli.registry :as malli.registry]
            [malli.experimental.time]))

(malli.registry/set-default-registry!
  (malli.registry/composite-registry
    (malli/default-schemas)
    (malli.time/schemas)))

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
