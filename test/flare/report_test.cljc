(ns flare.report-test
  (:require [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is deftest]]
            [flare.diff :refer [diff*]]
            [flare.generators :refer [distinct-values]]
            [flare.report :refer [report* report]]))

(deftest reports-swallows-exceptions-test
  (with-redefs [report* (fn [_] (throw (ex-info "what ever" {})))]
    (is (nil? (report :whatever)))))

(defspec generate-report-always-returns-non-empty-list-when-given-diffs
  100
  (prop/for-all [[a b] (distinct-values gen/any gen/any)]
                (not-empty (report (diff* a b)))))
