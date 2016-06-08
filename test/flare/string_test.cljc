(ns flare.string-test
  (:require [clojure.test :refer [deftest is testing]]
            [flare.string :refer [diff-string report-string-diff]]))

(def diff-recursive (constantly [:recursive-diff]))

(deftest string-diff-test

  (is (= (diff-string "foo" "fjoo" diff-recursive)
         [{:type :string
           :a->b [[:equal "f" :first]
                  [:insert "j"]
                  [:equal "oo" :last]]
           :b->a [[:equal "f" :first]
                  [:delete "j"]
                  [:equal "oo" :last]]
           :differences-count 1
           :similarity #?(:cljs 0.75 :clj 3/4)}])))


(deftest report-string-diff-test

  (testing "base case"
    (is (= (report-string-diff {:type :string
                                :a->b [[:equal "f" :first]
                                       [:insert "j"]
                                       [:equal "oo" :last]]
                                :b->a [[:equal "f" :first]
                                       [:delete "j"]
                                       [:equal "oo" :last]]
                                :differences-count 1
                                :similarity #?(:cljs 0.75 :clj 3/4)})
           ["strings have 1 difference (75% similarity)"
            "expected: \"f(-)oo\""
            "actual:   \"f(j)oo\""])))

  (testing "equal parts of longer strings get elided"
    (is (= (report-string-diff {:type :string
                                :a->b [[:equal "aaaaaaaaaaaaa " :first]
                                       [:change [[:insert "b"]
                                                 [:delete "f"]]]
                                       [:equal "oo aaaaaaaaaaaaaa "]
                                       [:change [[:insert "f"]
                                                 [:delete "b"]]]
                                       [:equal "ar aaaaaaaaaaaaa" :last]]
                                :b->a [[:equal "aaaaaaaaaaaaa " :first]
                                       [:change [[:insert "f"]
                                                 [:delete "b"]]]
                                       [:equal "oo aaaaaaaaaaaaaa "]
                                       [:change [[:insert "b"]
                                                 [:delete "f"]]]
                                       [:equal "ar aaaaaaaaaaaaa" :last]]
                                :differences-count 2
                                :similarity #?(:cljs 0.96 :clj 24/25)})
           ["strings have 2 differences (96% similarity)"
            "expected: \"...aaaaa (f)oo aaa...aaaaa (b)ar aaa...\""
            "actual:   \"...aaaaa (b)oo aaa...aaaaa (f)ar aaa...\""]))))
