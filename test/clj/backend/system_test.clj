(ns backend.system-test
  (:require [backend.api.todo :as todo]
            [backend.test-utils :as test-utils :refer [db]]
            [clojure.test :as t :refer [deftest is]]))

(t/use-fixtures :each test-utils/with-test-system)

(deftest example-test
  (is (empty? (:body (todo/get-todos {:db (db)}))))

  (is (= {:text "foobar"}
         (select-keys (:body (todo/create-todo {:db (db)
                                                :parameters {:body {:text "foobar"}}}))
                      [:text])))
  
  (is (= 1 (count (:body (todo/get-todos {:db (db)}))))))
