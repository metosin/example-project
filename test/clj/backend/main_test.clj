(ns backend.main-test
  (:require [backend.main :as sut]
            [clojure.test :as t :refer [deftest is]]))

(deftest system-config-test
  (is (= #{:adapter/jetty :web/routes :database/pool}
         (set (keys (sut/system-config))))))
