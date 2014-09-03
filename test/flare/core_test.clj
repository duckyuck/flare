(ns flare.core-test
  (:require [clojure.test :refer [is deftest testing]]
            [flare.core :refer :all])
  (:import [flare.core AtomDiff SetDiff MapKeysDiff StringDiffClansi]))

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

(deftest diff-test

  (testing "nil"

    (is (= (diff nil nil)
           nil))

    (is (= (diff nil 1)
           [(AtomDiff. nil 1)]))

    (is (= (diff 1 nil)
           [(AtomDiff. 1 nil)])))

  (testing "map"

    (is (= (diff {:a 1} {:a 1})
           nil))

    (is (= (diff {:a 1} nil)
           [(AtomDiff. {:a 1} nil)]))

    (is (= (diff {:a 1 :b 2} {:a 1 :c 3})
           [(MapKeysDiff. #{:b} nil)
            (MapKeysDiff. nil #{:c})]))

    (is (= (diff {:a 1 :b 2} {:a 1 :b 3})
           [{:b [(AtomDiff. 2 3)]}]))

    (is (= (diff {:a 1 :b 2 :c 1} {:a 1 :b 3 :d 1})
           [{:b [(AtomDiff. 2 3)]}
            (MapKeysDiff. #{:c} nil)
            (MapKeysDiff. nil #{:d})]))

    (is (= (diff {:a 1 :b 2 :c 4} {:a 1 :b 3 :c 5})
           [{:c [(AtomDiff. 4 5)]
             :b [(AtomDiff. 2 3)]}]))

    (is (= (diff {:a 1 :b {:c 2}} {:a 1 :b {:c 3}})
           [{:b [{:c [(AtomDiff. 2 3)]}]}])))

  (testing "sequence"

    (is (= (diff [0 1 2] [0 1 2])
           nil))

    (is (= (diff [0 1 2] nil)
           [(AtomDiff. [0 1 2] nil)]))

    (is (= (diff [0 1 2] [2 1 0])
           [{0 [(AtomDiff. 0 2)]
             2 [(AtomDiff. 2 0)]}]))

    (is (= (diff [0 1 2] [0 1 42])
           [{2 [(AtomDiff. 2 42)]}]))

    ;; FIXME - currently reports empty diff
    #_(is (not= (diff [0 1 2 3] [0 1 2])
              (VectorDiff. {}))))

  (testing "set"

    (is (= (diff #{1 2 3} #{1 2 3})
           nil))

    (is (= (diff #{1 2 3} nil)
           [(AtomDiff. #{1 2 3} nil)]))

    (is (= (diff #{1 2 3} #{1 2})
           [(SetDiff. #{3} nil)]))

    (is (= (diff #{1 2} #{1 2 3})
           [(SetDiff. nil #{3})]))

    (is (= (diff #{1 2 3} #{2 3 4})
           [(SetDiff. #{1} nil)
            (SetDiff. nil #{4})])))

  (testing "string"

    (is (= (diff "foo" "foo")
           nil))

    (is (= (diff "foo" nil)
           [(AtomDiff. "foo" nil)]))

    (is (= (diff "foo" "fjoo")
           [(StringDiffClansi. (clansi-diff "foo" "fjoo"))]))))

(deftest report-test

  (testing "atom"
    (is (= (report (AtomDiff. "a" 1))
           "expected: \"a\", was 1")))

  (testing "map entry"

    (testing "only in a"
      (is (= (report (MapKeysDiff. [:a] nil))
             "expected map to contain key: :a, but not found.")))

    (testing "nil keys"
      (is (= (report (MapKeysDiff. [nil] nil))
             "expected map to contain key: nil, but not found.")))

    (testing "only in b"
      (is (= (report (MapKeysDiff. nil [:b]))
             "map contained key: :b, but not expected."))))

  (testing "set"

    (testing "only in a"
      (is (= (report (SetDiff. [:a] nil))
             "expected set to contain: :a, but not found.")))

    (testing "only in b"
      (is (= (report (SetDiff. nil [:b]))
             "set contained: :b, but not expected.")))

    (testing "nil values"
      (is (= (report (SetDiff. [nil] nil))
             "expected set to contain: nil, but not found.")))))
