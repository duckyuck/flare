(ns flare.indexed-test
  (:require [clojure.test :refer [is deftest testing]]
            [flare.indexed :refer [flatten-indexed-diffs report-indexed-diff]]))

(deftest flatten-indexed-diffs-test

  (is (= (flatten-indexed-diffs
          [:x])
         {[] [:x]}))

  (is (= (flatten-indexed-diffs
          [:x :y])
         {[] [:x :y]}))

  (is (= (flatten-indexed-diffs
          [{:type :indexed
            :diff {:a [:x :y]}}])
         {[:a] [:x :y]}))

  (is (= (flatten-indexed-diffs
          [{:type :indexed
            :diff {:a [:x :y]
                   :b [:z]}}])
         {[:a] [:x :y]
          [:b] [:z]}))

  (is (= (flatten-indexed-diffs
          [:x
           {:type :indexed
            :diff {:a [:x :y]
                   :b [:z]}}])
         {[] [:x]
          [:a] [:x :y]
          [:b] [:z]}))

  (is (= (flatten-indexed-diffs
          [:x
           {:type :indexed
            :diff {:a [:foo
                       {:type :indexed
                        :diff {:x [:bar :y :baz]}}]
                   :b [:z]}}])
         {[] [:x]
          [:a] [:foo]
          [:a :x] [:bar :y :baz]
          [:b] [:z]}))

  #_(is (thrown? IllegalArgumentException
               (flatten-indexed-diffs
                1))))

(deftest report-indexed-diff-test

  (is (= (report-indexed-diff {:type :indexed
                               :diff {:a [:foo
                                          {:type :indexed
                                           :diff {:x [:bar :y :baz]}}]
                                      :b [:z]}} identity)
         ["in [:a] :foo"
          "in [:b] :z"
          "in [:a :x]\n  :bar\n  :y\n  :baz"])))
