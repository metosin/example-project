(ns frontend.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub ::todos
  :<- [:http/body [:todos]]
  (fn [todos _]
    (sort-by :id todos)))
