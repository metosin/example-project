(ns common.schema)

(def todo
  [:map
   [:text :string]
   [:status [:enum :resolved :unresolved]]])
