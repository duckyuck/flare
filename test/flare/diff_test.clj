(ns flare.diff-test
  (:require [flare.diff :refer :all]
            [flare.generators :refer [distinct-values]]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is deftest]]))

(defspec diff-never-returns-a-diff-when-inputs-are-equal
  100
  (prop/for-all [v gen/any]
                (= (diff v v) nil)))

;; TODO - Implement generator that generates similiar but unequal values. This test is probably futile.
(defspec diff-always-returns-a-diff-when-inputs-are-not-equal
  100
  (prop/for-all [[a b] (distinct-values gen/any gen/any)]
                (not (nil? (diff a b)))))
