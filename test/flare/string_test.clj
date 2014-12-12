(ns flare.string-test
  (:require [flare.string :refer :all]
            [flare.diff :as diff]
            [flare.report :as report]
            [clojure.test :refer [is deftest testing]])
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

  (testing "base case"
    (is (= (report (StringDiff. (create-string-diff
                                 "foo bar"
                                 "fool berr")))
          ["strings have 2 differences (66% similarity)"
           "expected: \"foo(-) b(a-)r\""
           "actual:   \"foo(l) b(er)r\""])))

  (testing "equal parts of longer strings get elided"
   (is (= (report (StringDiff. (create-string-diff
                                "aaaaaaaaaaaaa foo aaaaaaaaaaaaaa bar aaaaaaaaaaaaa"
                                "aaaaaaaaaaaaa boo aaaaaaaaaaaaaa far aaaaaaaaaaaaa")))
          ["strings have 2 differences (96% similarity)"
           "expected: \"...aaaaa (f)oo aaa...aaaaa (b)ar aaa...\""
           "actual:   \"...aaaaa (b)oo aaa...aaaaa (f)ar aaa...\""]))))
