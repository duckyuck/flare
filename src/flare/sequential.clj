(ns flare.sequential
  (:require [flare.diff :refer [diff*]]
            [flare.report :refer [Report]]
            [flare.util :refer [pluralize]]))

(defn report-sequential-count-diff
  [excess-idx only-in-a only-in-b]
  (let [missing-count (count (or (seq only-in-a) (seq only-in-b)))]
    [(str "expected length of sequence is " (+ (count only-in-a) excess-idx)
          ", actual length is " (+ (count only-in-b) excess-idx) ".")
     (if (seq only-in-a)
       (str "actual is missing " missing-count " " (pluralize only-in-a "element") ": " (pr-str only-in-a))
       (str "actual has " missing-count " " (pluralize only-in-b "element") " in excess: " (pr-str only-in-b)))]))

(defrecord SequentialSizeDiff [excess-idx only-in-a only-in-b]
  Report
  (report [_] (report-sequential-count-diff excess-idx only-in-a only-in-b)))

(defn diff-sequential-by-index
  [a b]
  (->> (map vector a b)
       (keep-indexed (fn [i [a b]] (when-not (= a b) [i (diff* a b)])))
       (into {})))

(defn diff-sequential-size
  [a b]
  (when-not (= (count a) (count b))
    (let [excess-idx (min (count a) (count b))]
      [(SequentialSizeDiff. excess-idx
        (drop excess-idx a)
        (drop excess-idx b))])))

(defn diff-sequential
  [a b]
  (remove empty? (cons (diff-sequential-by-index a b)
                       (diff-sequential-size a b))))
