(ns flare.demo
  (:require [flare.clojure-test :refer [install!]]
            [clojure.test :refer [is]]
            [clojure.pprint :as pp]
;            [clj-diff.core :as clj-diff]
            ))

(install!)


;; Numbers

(is (= 1 2))

(is (= 1 "1"))


;; Strings

(is (= "kodemaker for the win"
       "kdoemaker for teh win"))


;; Vector and lists

(is (= '() [1]))

(is (= [] '(1)))

(is (= [10 20 30]
       [10 25 30]))

(is (= [10 20 :a]
       [10 25 :b]))

(is (= ["foo" "bar" "baz" "kodemaker for the win" "wat"]
       ["foo" "bar" "bat" "kdoemaker for teh win" "wat"]))

(is (= [[1 2 3 4 5 6 7]]
       [[2 3 4 5 6 7 8]]))


;; Maps

(is (= {:a 1 :b 2}
       {:a 1 :b 3}))

(is (= {:a {:b "kodemaker"}
        :b {:c 1}
        :c {:x 2}}
       {:a {:b "kdoemaker"}
        :b {:c 2}
        :c {:y 3
            :z 4}}))


;; Sets

(is (= #{1 2 3} #{3 4 5}))

(is (= {:a {:b #{1 "foo" 2}}} {:a {:b #{2 "bar" 3}}}))


(comment
  ;; clj-diff

  (clj-diff/diff [:a :b :c] [:c :b :a])
  ;;=> {:+ [[-1 :c :b]], :- [1 2]}

  (clj-diff/diff [:a :b :c] [:a :b])
  ;;=> {:+ [], :- [2]}

  (clj-diff/diff [:a :b] [:a :b :c :d])
  ;;=> {:+ [[1 :c :d]], :- []}

  (clj-diff/diff [:b :c] [:a :b :c :d]))
 ;;=> {:+ [[-1 :a] [1 :d]], :- []}
