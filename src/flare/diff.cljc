(ns flare.diff
  (:require [clojure.data :refer [equality-partition]]
            [flare.atom :as atom]
            [flare.string :as string]
            [flare.map :as map]
            [flare.sequential :as sequential]
            [flare.set :as set]))

(defn compose-diffs
  [& diff-fns]
  (fn [a b diff-recursive-fn]
    (->> diff-fns
         (mapcat #(% a b diff-recursive-fn))
         (remove empty?))))

#?(:clj (defn resolve-diff-type [x]
          (condp instance? x
            java.util.List   :sequential
            java.lang.String :string
            java.util.Set    :set
            java.util.Map    :map
            :atom))
   :cljs (defn resolve-diff-type [x]
           (cond
             (array? x) :sequential
             (string? x) :string
             (map? x) :map
             (set? x) :set
             (implements? ISeqable x) :sequential
             :else :atom)))

(def differs
  {:sequential (compose-diffs sequential/diff-size sequential/diff-by-index)
   :map        (compose-diffs map/diff-values map/diff-keys)
   :set        set/diff-set
   :string     string/diff-string
   :atom       atom/diff-atom})

(declare diff*)

(defn diff-similar [a b]
  (let [diff-type (resolve-diff-type a)
        diff (get differs diff-type)]
    (diff a b diff*)))

(defn similar-types [a b]
  (when (= (equality-partition a) (equality-partition b))
    (if (string? a)
      (string? b)
      true)))

(defn diff*
  [a b]
  (when-not (= a b)
    (if (similar-types a b)
      (diff-similar a b)
      (atom/diff-atom a b nil))))

(defn diff
  [a b]
  (try
    (diff* a b)
    (catch Exception e)))
