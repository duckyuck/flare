(ns flare.sequential
  (:require [flare.indexed :as indexed]
            [flare.util :refer [pluralize]]))

(defn report-size-diff
  [{:keys [excess-idx only-in-a only-in-b]} & rest]
  (let [missing-count (count (or (seq only-in-a) (seq only-in-b)))]
    [(str "expected length of sequence is " (+ (count only-in-a) excess-idx)
          ", actual length is " (+ (count only-in-b) excess-idx) ".")
     (if (seq only-in-a)
       (str "actual is missing " missing-count " " (pluralize only-in-a "element") ": " (pr-str only-in-a))
       (str "actual has " missing-count " " (pluralize only-in-b "element") " in excess: " (pr-str only-in-b)))]))

(defn diff-size
  [a b _]
  (when-not (= (count a) (count b))
    (let [excess-idx (min (count a) (count b))]
      [{:type :sequential-size
        :excess-idx excess-idx
        :only-in-a (drop excess-idx a)
        :only-in-b (drop excess-idx b)}])))

(defn diff-by-index
  [a b diff-fn]
  [(indexed/indexed-diff
     (->> (map vector a b)
          (keep-indexed (fn [i [a b]] (when-not (= a b) [i (diff-fn a b)])))
          (into {})))])
