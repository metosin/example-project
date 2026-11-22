(ns frontend.http-fx-test
  (:require [cljs.test :as t :refer [deftest is]]
            [frontend.http-fx :as sut]))

(deftest read-transit-instant-test
  (let [x (sut/read-transit "[\"~#Instant\",\"2024-10-01T12:34:56.123456Z\"]")]
    (is (instance? js/Date x))
    (is (= "2024-10-01T12:34:56.123Z" (.toISOString x))))
  (let [d (:created-at (first (sut/read-transit "[[\"^ \",\"~:created-at\",[\"~#Instant\",\"2024-10-01T12:34:56Z\"]]]")))]
    (is (instance? js/Date d))
    (is (= "2024-10-01T12:34:56.000Z" (.toISOString d)))))
