(ns flare.report-test
  (:require [flare.report :refer :all]
            [clojure.test :refer [is deftest]]
            [clojure.test.check :as tc]
            [flare.diff :as diff]
            [clojure.test.check.clojure-test :refer [defspec]]
            [flare.generators :refer [distinct-values]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop])
  (:import [flare.atom AtomDiff]))

(def diff diff/diff)

(deftest flatten-keys-test

  (is (= (flatten-keys [1])
         {[] [1]}))

  (is (= (flatten-keys [{:a [1]}])
         {[:a] [1]}))

  (is (= (flatten-keys [{:a [1] :b [2]}])
         {[:a] [1]
          [:b] [2]}))

  (is (= (flatten-keys [1 {:a [2] :b [3]}])
         {[] [1]
          [:a] [2]
          [:b] [3]}))

  (is (= (flatten-keys [1 {:a [2] :b [3]} 4])
         {[] [1 4]
          [:a] [2]
          [:b] [3]}))

  (is (= (flatten-keys [{:a [1 {:b [2 {:c [3]}]}]}])
         {[:a] [1]
          [:a :b] [2]
          [:a :b :c] [3]}))

  (is (thrown? IllegalArgumentException
               (flatten-keys 1))))

(deftest generate-report-for-keyed-diff-test
  (is (= (generate-report-for-keyed-diff [[:a :b :c] [(AtomDiff. 1 2)]])
         "in [:a :b :c] expected 1, was 2")))

(deftest generate-reports-swallows-exceptions-test
  (with-redefs [generate-reports* #(throw (Exception.))]
    (is (nil? (generate-reports :whatever)))))

(defspec generate-report-always-returns-non-empty-list-when-given-diffs
  100
  (prop/for-all [[a b] (distinct-values gen/any gen/any)]
                (not-empty (generate-reports* (diff a b)))))
