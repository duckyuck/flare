(ns flare.foo-test
  (:require [flare.atom :refer [diff-atom report-atom-diff]])
  (:require-macros [cljs.test :refer [deftest testing is]]))

(deftest foo-test-fail
  (is (= 42 43)))


(deftest foo-test
  (is (= 1 3)))

(deftest map-test
  (is (= {:a 1} {:a 2})))

(deftest foo-test-2
  (is (= (diff-atom "foo" 1 nil)
         [{:type :atom
           :a "foo"
           :b 2}])))

