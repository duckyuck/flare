(ns flare.set-test
  (:require [clojure.test :refer [deftest is]]
            #?(:clj flare.clojure-test)
            [flare.set :refer [diff-set report-set]]))

(deftest diff-set-test

  (is (= (diff-set #{1 2 3} #{1 2})
         [{:type :set
           :only-in :a
           :values #{3}}]))

  (is (= (diff-set #{1 2} #{1 2 3})
         [{:type :set
           :only-in :b
           :values #{3}}]))

  (is (= (diff-set #{1 2 3} #{2 3 4})
         [{:type :set
           :only-in :a
           :values #{1}}
          {:type :set
           :only-in :b
           :values #{4}}])))

(deftest report-set-test

  (is (= (report-set {:type :set
                      :only-in :a
                      :values [:x]})
         ["expected set to contain: :x, but not found."]))

  (is (= (report-set {:type :set
                      :only-in :b
                      :values [:x]})
         ["set contained: :x, but not expected."])))
