(ns flare.sequential-test
  (:require [clojure.test :refer [deftest is]]
            [flare.sequential :refer [diff-by-index diff-size report-size-diff]]))

(def diff-recursive (constantly [:recursive-diff]))

(deftest diff-size-test

  (is (= (diff-size [0 1 2] [0 1 2] diff-recursive)
         nil))

  (is (= (diff-size [0 1 2] [2 1 0] diff-recursive)
         nil))

  (is (= (diff-size [:a :b] [:a :b :c :d] diff-recursive)
         [{:type :sequential-size
           :excess-idx 2
           :only-in-a []
           :only-in-b [:c :d]}]))

  (is (= (diff-size [:a :b :c :d] [:a :b] diff-recursive)
         [{:type :sequential-size
           :excess-idx 2
           :only-in-a [:c :d]
           :only-in-b []}])))

(deftest diff-by-index-test

  (is (= (diff-by-index [0 1 2] [2 1 0] diff-recursive)
         [{:type :indexed
           :diff {0 [:recursive-diff]
                  2 [:recursive-diff]}}]))

  (is (= (diff-by-index [0 1 2] [0 1 42] diff-recursive)
         [{:type :indexed
           :diff {2 [:recursive-diff]}}]))

  (is (= (diff-by-index [:x :b :c :d] [:a :b] diff-recursive)
         [{:type :indexed
           :diff {0 [:recursive-diff]}}])))

(deftest report-sequential-test

  (is (= (report-size-diff
          {:type :sequential-size
           :excess-idx 3
           :only-in-a [:a :b]
           :only-in-b []})
         ["expected length of sequence is 5, actual length is 3."
          "actual is missing 2 elements: [:a :b]"]))

  (is (= (report-size-diff
          {:type :sequential-size
           :excess-idx 3
           :only-in-a []
           :only-in-b [:a :b]})
         ["expected length of sequence is 3, actual length is 5."
          "actual has 2 elements in excess: [:a :b]"])))
