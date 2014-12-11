(ns flare.string-test
  (:require [flare.string :refer :all]
            [flare.diff :as diff]
            [flare.report :as report]
            [clojure.test :refer [is deftest]])
  (:import [flare.string StringDiff]
           [flare.atom AtomDiff]))

(def diff diff/diff*)
(def report report/report)

(deftest string-diff-test

  (is (= (diff "foo" "foo")
         nil))

  (is (= (diff "foo" nil)
         [(AtomDiff. "foo" nil)]))

  (is (= (diff "foo" "fjoo")
         [(StringDiff. (create-string-diff "foo" "fjoo"))])))

(deftest string-report-test

  (is (= (report (StringDiff. (create-string-diff "foo bar" "fool berr")))
         ["strings have 2 differences (66% similarity)"
          "expected: \"foo(-) b(a-)r\""
          "actual:   \"foo(l) b(er)r\""])))
