(ns flare.sequential-test
  (:require [flare.sequential :refer :all]
            [flare.diff :as diff]
            [flare.report :as report]
            [clojure.test :refer [is deftest]])
  (:import [flare.sequential SequentialSizeDiff]
           [flare.atom AtomDiff]))

(def diff diff/diff*)
(def report report/report)

(deftest diff-sequential-test

  (is (= (diff [0 1 2] [0 1 2])
         nil))

  (is (= (diff [0 1 2] nil)
         [(AtomDiff. [0 1 2] nil)]))

  (is (= (diff [0 1 2] [2 1 0])
         [{0 [(AtomDiff. 0 2)]
           2 [(AtomDiff. 2 0)]}]))

  (is (= (diff [0 1 2] [0 1 42])
         [{2 [(AtomDiff. 2 42)]}]))

  (is (= (diff [:a :b] [:a :b :c :d])
         [(SequentialSizeDiff. 2 [] [:c :d])]))

  (is (= (diff [:a :b :c :d] [:a :b])
         [(SequentialSizeDiff. 2 [:c :d] [])]))

  (is (= (diff [:x :b :c :d] [:a :b])
         [(SequentialSizeDiff. 2 [:c :d] [])
          {0 [(AtomDiff. :x :a)]}])))

(deftest report-sequential-test

  (is (= (report (SequentialSizeDiff. 3 [:a :b] []))
         ["expected length of sequence is 5, actual length is 3."
          "actual is missing 2 elements: [:a :b]"]))

  (is (= (report (SequentialSizeDiff. 3 [] [:a :b]))
         ["expected length of sequence is 3, actual length is 5."
          "actual has 2 elements in excess: [:a :b]"])))
