(ns frontend.main-test
  (:require [cljs.test :as t :refer [deftest is]]
            [frontend.core :as sut]))

(deftest init-test
  (is (fn? sut/init)))
