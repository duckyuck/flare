(ns flare.map-test
  (:require [flare.map :refer :all]
            [flare.diff :as diff]
            [flare.report :as report]
            [clojure.test :refer [is deftest testing]])
  (:import [flare.atom AtomDiff]
           [flare.map MapKeysDiff]))

(def diff diff/diff*)
(def report report/report)

(deftest diff-map-test

  (is (= (diff {:a 1} {:a 1})
         nil))

  (is (= (diff {:a 1} nil)
         [(AtomDiff. {:a 1} nil)]))

  (is (= (diff {:a 1 :b 2} {:a 1 :c 3})
         [(MapKeysDiff. #{:b} nil)
          (MapKeysDiff. nil #{:c})]))

  (is (= (diff {:a 1 :b 2} {:a 1 :b 3})
         [{:b [(AtomDiff. 2 3)]}]))

  (is (= (diff {:a 1 :b 2 :c 1} {:a 1 :b 3 :d 1})
         [{:b [(AtomDiff. 2 3)]}
          (MapKeysDiff. #{:c} nil)
          (MapKeysDiff. nil #{:d})]))

  (is (= (diff {:a 1 :b 2 :c 4} {:a 1 :b 3 :c 5})
         [{:c [(AtomDiff. 4 5)]
           :b [(AtomDiff. 2 3)]}]))

  (is (= (diff {:a 1 :b {:c 2}} {:a 1 :b {:c 3}})
         [{:b [{:c [(AtomDiff. 2 3)]}]}])))

(deftest report-map-test

  (is (= (report (MapKeysDiff. [:a] nil))
         "expected map to contain key: :a, but not found."))

  (is (= (report (MapKeysDiff. [nil] nil))
         "expected map to contain key: nil, but not found."))

  (is (= (report (MapKeysDiff. nil [:b]))
         "map contained key: :b, but not expected.")))
