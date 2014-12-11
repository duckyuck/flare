(ns flare.set-test
  (:require [flare.set :refer :all]
            [flare.diff :as diff]
            [flare.report :as report]
            [clojure.test :refer [is deftest]])
  (:import [flare.atom AtomDiff]
           [flare.set SetDiff]))

(def diff diff/diff*)
(def report report/report)

(deftest diff-set-test

  (is (= (diff #{1 2 3} #{1 2 3})
         nil))

  (is (= (diff #{1 2 3} nil)
         [(AtomDiff. #{1 2 3} nil)]))

  (is (= (diff #{1 2 3} #{1 2})
         [(SetDiff. #{3} nil)]))

  (is (= (diff #{1 2} #{1 2 3})
         [(SetDiff. nil #{3})]))

  (is (= (diff #{1 2 3} #{2 3 4})
         [(SetDiff. #{1} nil)
          (SetDiff. nil #{4})])))

(deftest report-set-test

  (is (= (report (SetDiff. [:a] nil))
         "expected set to contain: :a, but not found."))

  (is (= (report (SetDiff. nil [:b]))
         "set contained: :b, but not expected."))

  (is (= (report (SetDiff. [nil] nil))
         "expected set to contain: nil, but not found.")))
