(ns frontend.db)

(def default-db
  {:todos (sorted-map-by >)})
