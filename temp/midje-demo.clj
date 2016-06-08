(ns midje-demo
  (:require [flare.midje :refer [install!]]
            [midje.sweet :refer :all]))

(install!)






;; Numbers

(fact 1 => 2)

(fact 1 => "1")










;; Strings

(fact "kodemaker for the win"
      => "kdoemaker for teh win")




(fact (inc 32) => even?)








;; Vector and lists

(fact [10 20 30]
      => [10 25 30])

(fact [10 20 :a]
      => [10 25 :b])

(fact ["foo" "bar" "baz" "kodemaker for the win" "wat"]
      => ["foo" "bar" "bat" "kdoemaker for teh win" "wat"])













;; Maps

(fact {:a 1 :b 2}
      => {:a 1 :b 3})

(fact {:a {:b "kodemaker"}
         :b {:c 1}
         :c {:x 2}}
      => {:a {:b "kdoemaker"}
       :b {:c 2}
       :c {:y 3
           :z 4}})


(def a {:a 1})

(fact a => {:b 2})










;; Sets

(fact #{1 2 3} => #{3 4 5})

(fact {:a {:b #{1 "foo" 2}}} => {:a {:b #{2 "bar" 3}}})


(fact '() => [1])

(fact [] => '(1))
