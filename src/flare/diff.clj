(ns flare.diff
  (:require [flare.atom :refer [diff-atom]]
            [clojure.data :refer [equality-partition]]))

(defprotocol Diff
  (diff-similar [a b]))

(defn compose-diffs
  [& diff-fns]
  (fn [a b]
    (remove empty? (mapcat #(% a b) diff-fns))))

(defn diff*
  [a b]
  (when-not (= a b)
    (if (= (equality-partition a) (equality-partition b))
      (diff-similar a b)
      (diff-atom a b))))

(defn diff
  [a b]
  (try
    (diff* a b)
    (catch Exception e)))
