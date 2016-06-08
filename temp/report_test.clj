(ns flare.report-test
  (:require [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is deftest]]
            [flare.diff :as diff]
            [flare.generators :refer [distinct-values]]
            [flare.report :refer :all])
  (:import [flare.atom AtomDiff]))

(def diff diff/diff)

(deftest reports-swallows-exceptions-test
  (with-redefs [generate-reports* #(throw (ex-info "what ever" {}))]
    (is (nil? (generate-reports :whatever)))))

(defspec generate-report-always-returns-non-empty-list-when-given-diffs
  100
  (prop/for-all [[a b] (distinct-values gen/any gen/any)]
                (not-empty (generate-reports* (diff a b)))))
