(ns flare.atom-test
  (:require [clojure.test :refer [deftest is]]
            [flare.atom :refer [diff-atom report-atom-diff]]))

(deftest diff-atom-test

  (is (= (diff-atom "foo" 1 nil)
         [{:type :atom
           :a "foo"
           :b 1}])))

(deftest report-atom-test

  (is (= (report-atom-diff {:type :atom
                            :a "foo"
                            :b 1})
         ["expected \"foo\", was 1"])))
