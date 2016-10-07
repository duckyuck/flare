(ns flare.diff-test
  (:require [flare.diff :as sut]
            [flare.generators :refer [distinct-values]]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is deftest]]
            [flare.report :as report]))

(deftest diff-swallows-exceptions-test
  (with-redefs [sut/diff* (fn [_ _] (throw (ex-info "what ever" {})))]
    (is (nil? (sut/diff :what :ever)))))

(defspec diff-never-returns-a-diff-when-inputs-are-equal
  100
  (prop/for-all [v gen/any]
                (= (sut/diff* v v) nil)))

;; TODO - Implement generator that generates similiar but unequal values. This test is probably futile.
(defspec diff-always-returns-a-diff-when-inputs-are-not-equal
  100
  (prop/for-all [[a b] (distinct-values gen/any gen/any)]
                (not (nil? (sut/diff* a b)))))
