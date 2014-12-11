(ns flare.atom-test
  (:require [flare.atom :refer :all]
            [flare.diff :as diff]
            [flare.report :as report]
            [clojure.test :refer [is deftest]])
  (:import [flare.atom AtomDiff]))

(def diff diff/diff*)
(def report report/report)

(deftest diff-atom-test

  (is (= (diff nil nil)
         nil))

  (is (= (diff nil 1)
         [(AtomDiff. nil 1)]))

  (is (= (diff 1 nil)
         [(AtomDiff. 1 nil)]))

  (is (= (diff 1 2)
         [(AtomDiff. 1 2)])))

(deftest report-atom-test

  (is (= (report (AtomDiff. "a" 1))
         "expected \"a\", was 1")))
