(ns flare.atom
  (:require [flare.report :refer [Report]]))

(defrecord AtomDiff [a b]
  Report
  (report [_] (str "expected " (pr-str a) ", was " (pr-str b))))

(defn diff-atom
  [a b]
  [(AtomDiff. a b)])
