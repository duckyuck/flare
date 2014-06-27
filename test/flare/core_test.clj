(ns flare.core-test
  (:require [clojure.test :refer [is deftest testing]]
            [flare.core :refer :all])
  (:import [flare.core AtomDiff VectorDiff MapDiffs SetDiff MapEntryDiff MapValueDiff]))

(deftest diff-test

  (testing "nil"

    (is (= (diff nil nil)
           nil))

    (is (= (diff nil 1)
           (AtomDiff. nil 1)))

    (is (= (diff 1 nil)
           (AtomDiff. 1 nil))))

  (testing "map"

    (is (= (diff {:a 1} {:a 1})
           nil))

    (is (= (diff {:a 1} nil)
           (AtomDiff. {:a 1} nil)))

    (is (= (diff {:a 1 :b 2} {:a 1 :c 3})
           (MapDiffs. #{(MapEntryDiff. #{:b} #{:c})})))

    (is (= (diff {:a 1 :b 2} {:a 1 :b 3})
           (MapDiffs. #{(MapValueDiff. :b (AtomDiff. 2 3))})))

    (is (= (diff {:a 1 :b 2 :c 1} {:a 1 :b 3 :d 1})
           (MapDiffs. #{(MapEntryDiff. #{:c} #{:d})
                        (MapValueDiff. :b (AtomDiff. 2 3))})))

    (is (= (diff {:a 1 :b 2 :c 4} {:a 1 :b 3 :c 5})
           (MapDiffs. #{(MapValueDiff. :c (AtomDiff. 4 5))
                        (MapValueDiff. :b (AtomDiff. 2 3))})))

    (is (= (diff {:a 1 :b {:c 2}} {:a 1 :b {:c 3}})
           (MapDiffs. #{(MapValueDiff. :b (MapDiffs. #{(MapValueDiff. :c (AtomDiff. 2 3))}))}))))

  (testing "sequence"

    (is (= (diff [0 1 2] [0 1 2])
           nil))

    (is (= (diff [0 1 2] nil)
           (AtomDiff. [0 1 2] nil)))

    (is (= (diff [0 1 2] [2 1 0])
           (VectorDiff. {0 (AtomDiff. 0 2)
                         2 (AtomDiff. 2 0)})))

    (is (= (diff [0 1 2] [0 1 42])
           (VectorDiff. {2 (AtomDiff. 2 42)})))

    ;; FIXME - currently reports empty diff
    #_(is (not= (diff [0 1 2 3] [0 1 2])
              (VectorDiff. {}))))

  (testing "set"

    (is (= (diff #{1 2 3} #{1 2 3})
           nil))

    (is (= (diff #{1 2 3} nil)
           (AtomDiff. #{1 2 3} nil)))

    (is (= (diff #{1 2 3} #{1 2})
           (SetDiff. #{3} #{})))

    (is (= (diff #{1 2} #{1 2 3})
           (SetDiff. #{} #{3})))

    (is (= (diff #{1 2 3} #{2 3 4})
           (SetDiff. #{1} #{4}))))

  (testing "string"

    (is (= (diff "foo" "foo")
           nil))

    (is (= (diff "foo" nil)
           (AtomDiff. "foo" nil)))))

(def mock-report (reify Report (report [_] "mock report")))

(deftest report-test

  (testing "atom"
    (is (= (report (AtomDiff. "a" 1))
           ["expected: \"a\", was 1"])))

  (testing "vector"
    (is (= (report (VectorDiff. {0 mock-report, 1 mock-report }))
           ["0 mock report" "1 mock report"])))

  (testing "map"
    (is (= (report (MapDiffs. [mock-report mock-report]))
           ["mock report" "mock report"])))

  (testing "map entry"

    (testing "only in a"
      (is (= (report (MapEntryDiff. [:a] nil))
             "expected to contain key: :a, but not found.")))

    (testing "only in b"
      (is (= (report (MapEntryDiff. nil [:b]))
             "contained key: :b, but not expected")))

    (testing "multiple keys"
      (is (= (report (MapEntryDiff. [:a :b] [:c :d]))
             "expected to contain keys: [:a :b], but not found. contained keys: [:c :d], but not expected"))))

  (testing "map value"
    (is (= (report (MapValueDiff. :a mock-report))
           ":a mock report")))

  (testing "set"

    (testing "only in a"
      (is (= (report (SetDiff. [:a] nil))
             ["expected to contain: :a, but not found."])))

    (testing "only in b"
      (is (= (report (SetDiff. nil [:b]))
             ["contained: :b, but not expected"])))

    (testing "multiple keys"
      (is (= (report (SetDiff. [:a :b] [:c :d]))
             ["expected to contain: [:a :b], but not found. contained: [:c :d], but not expected"])))))
