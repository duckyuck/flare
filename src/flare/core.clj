(ns flare.core
  (:require [clojure.data :refer [equality-partition]]
            [clansi :as clansi])
  (:import [clojure.lang PersistentHashSet PersistentArrayMap IPersistentVector]
           [name.fraser.neil.plaintext diff_match_patch$Operation]
           [name.fraser.neil.plaintext diff_match_patch]))

(defprotocol Diff
  (diff-similar [a b]))

(defprotocol Report
  (report [diff]))

(declare diff-atom)

(defn flatten-when-single
  [coll]
  (if (= 1 (count coll))
    (first coll)
    coll))

(defn pluralize
  [s n]
  (if (and (sequential? n) (< 1 (count n)))
    (str s "s")
    s))

(defn diff
  [a b]
  (when-not (= a b)
    (if (= (equality-partition a) (equality-partition b))
      (diff-similar a b)
      (diff-atom a b))))

;; Atom

(defrecord AtomDiff [a b]
  Report
  (report [_] [(str "expected: " (pr-str a) ", was " (pr-str b))]))

(defn diff-atom
  [a b]
  (AtomDiff. a b))

;; Set

(defn report-set
  [only-in-a only-in-b]
  [(clojure.string/join
    " "
    (-> []
        (cond-> (seq only-in-a) (conj (str "expected to contain: " (flatten-when-single only-in-a) ", but not found.")))
        (cond-> (seq only-in-b) (conj (str "contained: " (flatten-when-single only-in-b) ", but not expected")))))])

(defrecord SetDiff [only-in-a only-in-b]
  Report
  (report [_] (report-set only-in-a only-in-b)))

(defn diff-set
  [a b]
  (SetDiff. (clojure.set/difference a b) (clojure.set/difference b a)))

;; Map

(defn report-map-entry-diff
  [only-in-a only-in-b]
  (clojure.string/join
   " "
   (-> []
       (cond-> (seq only-in-a) (conj (str "expected to contain " (pluralize "key" only-in-a) ": "
                                          (flatten-when-single only-in-a) ", but not found.")))
       (cond-> (seq only-in-b) (conj (str "contained " (pluralize "key" only-in-b) ": "
                                          (flatten-when-single only-in-b) ", but not expected"))))))

(defrecord MapEntryDiff [only-in-a only-in-b]
  Report
  (report [_] (report-map-entry-diff only-in-a only-in-b)))

(defn map-value-diff-report
  [k v]
  (apply str k " "  (report v)))

(defrecord MapValueDiff [k v]
  Report
  (report [_] (map-value-diff-report k v)))

(defn diff-map-keys
  [a b]
  (let [a-keys (set (keys a))
        b-keys (set (keys b))]
    (when (not= a-keys b-keys)
      (MapEntryDiff. (clojure.set/difference a-keys b-keys)
                     (clojure.set/difference b-keys a-keys)))))

(defn diff-map-values
  [a b]
  (->> (clojure.set/intersection (set (keys a)) (set (keys b)))
       (map (fn [k] [k (diff (a k) (b k))]))
       (filter second)
       (map (fn [[k v]] (MapValueDiff. k v)))))

(defrecord MapDiffs [diffs]
  Report
  (report [_] (map report diffs)))

(defn diff-map
  [a b]
  (let [diffs (remove nil? (cons (diff-map-keys a b)
                                 (diff-map-values a b)))]
    (when-not (empty? diffs)
      (MapDiffs. (set diffs)))))


;; Vector

(defn report-vector
  [diff]
  (map (fn [[k v]] (apply str k " "  (report v))) diff))

(defrecord VectorDiff [diff]
  Report
  (report [_] (report-vector diff)))

(defn diff-sequential
  [a b]
  (VectorDiff. (->> (map vector a b)
                    (map-indexed (fn [i [a b]] (when (not= a b) [i (diff a b)])))
                    (remove nil?)
                    (into {}))))


;; String

(def diff-colors
  {diff_match_patch$Operation/EQUAL :reset
   diff_match_patch$Operation/INSERT :cyan
   diff_match_patch$Operation/DELETE :red})

(defn report-string-clansi
  [diff]
  [(->> diff
        (map #(clansi/style (.text %) (diff-colors (.operation %))))
        (apply str "strings differ: "))])

(defrecord StringDiffClansi [diff]
  Report
  (report [_] (report-string-clansi diff)))

(defn diff-string
  [a b]
  (if (= (type b) String)
    (StringDiffClansi. (.diff_main (diff_match_patch.) a b))
    (diff-atom a b)))

(extend-protocol Diff
  nil
  (diff-similar [a b]
    (diff-atom a b))

  java.util.Set
  (diff-similar [a b]
    (diff-set a b))

  java.util.Map
  (diff-similar [a b]
    (diff-map a b))

  java.util.List
  (diff-similar [a b]
    (diff-sequential a b))

  java.lang.Object
  (diff-similar [a b]
    (diff-atom a b))

  java.lang.String
  (diff-similar [a b]
    (diff-string a b)))
