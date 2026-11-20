(ns frontend.main-test
  (:require [cljs.test :as t :refer [deftest is]]
            [frontend.core :as sut]))

(deftest init-test
  (is (fn? sut/init)))

(deftest example-test
  (is (= 1 1)))
