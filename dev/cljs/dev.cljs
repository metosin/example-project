(ns dev
  (:require [reagent-dev-tools.core :as dev-tools]
            [re-frame.db]))

(dev-tools/start! {:state-atom re-frame.db/app-db})
