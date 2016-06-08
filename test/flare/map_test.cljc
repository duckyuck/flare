(ns flare.map-test
  (:require [clojure.test :refer [deftest is testing]]
            [flare.map :refer [diff-keys diff-values report-keys-diff]]))

(def diff-recursive (constantly [:recursive-diff]))

(deftest diff-keys-test

  (is (= (diff-keys {:x 1 :y 2} {:x 1 :y 3} nil)
         nil))

  (is (= (diff-keys {:x 1 :y 2} {:x 1} nil)
         [{:type :keys :only-in :a :keys #{:y}}]))

  (is (= (diff-keys {:x 1} {:x 1 :z 3} nil)
         [{:type :keys :only-in :b :keys #{:z}}]))

  (is (= (diff-keys {:x 1 :y 2} {:x 1 :z 3} nil)
         [{:type :keys :only-in :a :keys #{:y}}
          {:type :keys :only-in :b :keys #{:z}}])))

(deftest diff-values-test

  (is (= (diff-values {:x 1 :y 2} {:x 1 :y 3 :z 4} diff-recursive)
         [{:type :indexed
           :diff {:x [:recursive-diff]
                  :y [:recursive-diff]}}]))

  (is (= (diff-values {:x 1 :y 2 :z 4} {:x 1 :y 3} diff-recursive)
         [{:type :indexed
           :diff {:x [:recursive-diff]
                  :y [:recursive-diff]}}])))

(deftest report-map-test

  (is (= (report-keys-diff {:type :keys
                            :only-in :a
                            :keys #{:x}})
         "expected map to contain key: :x, but not found."))

  (is (= (report-keys-diff {:type :keys
                            :only-in :a
                            :keys #{:x :y}})
         "expected map to contain keys: #{:y :x}, but not found."))

  (is (= (report-keys-diff {:type :keys
                            :only-in :b
                            :keys #{:x}})
         "map contained key: :x, but not expected."))

  (is (= (report-keys-diff {:type :keys
                            :only-in :b
                            :keys #{:x :y}})
         "map contained keys: #{:y :x}, but not expected.")))
