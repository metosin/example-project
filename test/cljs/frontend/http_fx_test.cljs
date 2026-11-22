(ns frontend.http-fx-test
  (:require [cljs.test :as t :refer [deftest is]]
            [frontend.http-fx :as sut]))

(deftest read-transit-instant-test
  (let [x (sut/read-transit "[\"~#Instant\",\"2024-10-01T12:34:56.123456Z\"]")]
    (is (instance? js/Date x))
    (is (= "2024-10-01T12:34:56.123Z" (.toISOString x))))
  (is (= [{:created-at "2024-10-01T12:34:56.000Z"}]
         (map #(update % :created-at (fn [d] (.toISOString d)))
              (sut/read-transit "[[\"^ \",\"~:created-at\",[\"~#Instant\",\"2024-10-01T12:34:56Z\"]]]")))))
