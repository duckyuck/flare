(ns flare.core
  (:require [clojure.data :refer [equality-partition]]
            [clansi :as clansi])
  (:import [clojure.lang PersistentHashSet PersistentArrayMap]
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

(defn map-and-not-report?
  [m]
  (and (map? m) (not (satisfies? Report m))))

(defn flatten-keys
  ([coll]
     (flatten-keys {} [] coll))
  ([a ks coll]
     (cond
      (map-and-not-report? coll) (reduce into
                                         (map (fn [[k v]]
                                                (flatten-keys a (conj ks k) v))
                                              (seq coll)))
      (sequential? coll) (let [groups (group-by map-and-not-report? coll)
                           m (first (groups true))
                           rest (groups false)]
                       (-> a
                           (cond-> m (flatten-keys ks m))
                           (cond-> (seq rest) (assoc ks rest))))
      :else (throw (IllegalArgumentException. "coll must be vector or map")))))

(defn join-with-newlines
  [coll]
  (clojure.string/join "\n" coll))

(defn generate-report-for-keyed-diff
  [[path diffs]]
  (let [diffs (map report diffs)
        indent-diffs? (and (seq path) (< 1 (count diffs)))]
    (-> diffs
        (cond->> indent-diffs? (map #(str "  " %)))
        join-with-newlines
        (cond->> (seq path) (str "in " (pr-str path) (if indent-diffs? "\n" " "))))))

(defn generate-reports
  [diffs]
  (->> diffs
       flatten-keys
       (map generate-report-for-keyed-diff)
       (clojure.string/join "\n")))


;; Atom

(defrecord AtomDiff [a b]
  Report
  (report [_] (str "expected: " (pr-str a) ", was " (pr-str b))))

(defn diff-atom
  [a b]
  [(AtomDiff. a b)])

;; Set

(defn report-set
  [only-in-a only-in-b]
  (if (seq only-in-a)
    (str "expected set to contain: "
         (pr-str (flatten-when-single only-in-a))
         ", but not found.")
    (str "set contained: "
         (pr-str (flatten-when-single only-in-b))
         ", but not expected.")))

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

;; Map

(defn report-map-keys-diff
  [only-in-a only-in-b]
  (if (seq only-in-a)
    (str "expected map to contain " (pluralize "key" only-in-a) ": "
         (pr-str (flatten-when-single only-in-a))
         ", but not found.")
    (str "map contained " (pluralize "key" only-in-b) ": "
         (pr-str (flatten-when-single only-in-b))
         ", but not expected.")))

(defrecord MapKeysDiff [only-in-a only-in-b]
  Report
  (report [_] (report-map-keys-diff only-in-a only-in-b)))

(defn diff-map-keys
  [a b]
  (let [a-keys (set (keys a))
        b-keys (set (keys b))]
    (when (not= a-keys b-keys)
      (let [only-in-a (clojure.set/difference a-keys b-keys)
            only-in-b (clojure.set/difference b-keys a-keys)]
        (-> []
            (cond-> (seq only-in-a) (conj (MapKeysDiff. only-in-a nil)))
            (cond-> (seq only-in-b) (conj (MapKeysDiff. nil only-in-b))))))))

(defn diff-map-values
  [a b]
  (->> (clojure.set/intersection (set (keys a)) (set (keys b)))
       (map (fn [k] [k (diff (get a k) (get b k))]))
       (filter second)
       (into {})))

(defn diff-map
  [a b]
  (remove empty? (cons (diff-map-values a b)
                       (diff-map-keys a b))))

;; Sequential

(defn diff-sequential
  [a b]
  [(->> (map vector a b)
        (keep-indexed (fn [i [a b]] (when (not= a b) [i (diff a b)])))
        (into {}))])

;; String

(def diff-colors
  {diff_match_patch$Operation/EQUAL :reset
   diff_match_patch$Operation/INSERT :cyan
   diff_match_patch$Operation/DELETE :red})

(defn report-string-clansi
  [diff]
  (->> diff
       (map #(clansi/style (.text %) (diff-colors (.operation %))))
       (apply str "strings differ: ")))

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
