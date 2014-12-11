(ns flare.set
  (:require [flare.report :refer [Report]]
            [flare.util :refer [flatten-when-single]]))

(defn report-set
  [only-in-a only-in-b]
  (if (seq only-in-a)
    (str "expected set to contain: "(pr-str (flatten-when-single only-in-a)) ", but not found.")
    (str "set contained: " (pr-str (flatten-when-single only-in-b)) ", but not expected.")))

(defrecord SetDiff [only-in-a only-in-b]
  Report
  (report [_] (report-set only-in-a only-in-b)))

(defn diff-set
  [a b]
  (let [only-in-a (clojure.set/difference a b)
        only-in-b (clojure.set/difference b a)]
    (-> []
        (cond-> (seq only-in-a) (conj (SetDiff. only-in-a nil)))
        (cond-> (seq only-in-b) (conj (SetDiff. nil only-in-b))))))
