(ns flare.core
  (:require [clojure.data :refer [equality-partition]])
  (:import [name.fraser.neil.plaintext diff_match_patch]
           [name.fraser.neil.plaintext diff_match_patch$Operation]))

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
  [s coll]
  (if (and (sequential? coll) (< 1 (count coll)))
    (str s "s")
    s))

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
  (let [reports (flatten (map report diffs))
        indent-reports? (and (seq path) (< 1 (count reports)))]
    (-> reports
        (cond->> indent-reports? (map #(str "  " %)))
        join-with-newlines
        (cond->> (seq path) (str "in " (pr-str path) (if indent-reports? "\n" " "))))))

(defn generate-reports*
  [diffs]
  (->> diffs
       flatten-keys
       sort
       (map generate-report-for-keyed-diff)))

(defn generate-reports
  [diffs]
  (try
    (generate-reports diffs)
    (catch Exception e)))

;; Atom

(defrecord AtomDiff [a b]
  Report
  (report [_] (str "expected " (pr-str a) ", was " (pr-str b))))

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

(defn report-sequential-count-diff
  [excess-idx only-in-a only-in-b]
  (let [missing-count (count (or (seq only-in-a) (seq only-in-b)))]
    [(str "expected length of sequence is " (+ (count only-in-a) excess-idx)
          ", actual length is " (+ (count only-in-b) excess-idx) ".")
     (if (seq only-in-a)
       (str "actual is missing " missing-count " " (pluralize "element" only-in-a) ": " (pr-str only-in-a))
       (str "actual has " missing-count " " (pluralize "element" only-in-b) " in excess: " (pr-str only-in-b)))]))

(defrecord SequentialSizeDiff [excess-idx only-in-a only-in-b]
  Report
  (report [_] (report-sequential-count-diff excess-idx only-in-a only-in-b)))

(defn diff-sequential-by-index
  [a b]
  (->> (map vector a b)
       (keep-indexed (fn [i [a b]] (when-not (= a b) [i (diff a b)])))
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

;; String

(defn string->dash
  [s]
  (apply str (repeat (count s) "-")))

(defn enclose-in-parenthesis
  [s]
  (str "(" s ")"))

(def diff-conversions
  {diff_match_patch$Operation/EQUAL identity
   diff_match_patch$Operation/INSERT (comp enclose-in-parenthesis identity)
   diff_match_patch$Operation/DELETE (comp enclose-in-parenthesis string->dash)})

(defn flip-insert-delete
  [operation]
  (condp = operation
    diff_match_patch$Operation/INSERT diff_match_patch$Operation/DELETE
    diff_match_patch$Operation/DELETE diff_match_patch$Operation/INSERT
    operation))

(defn diff-operation
  [flip-insert-delete? diff]
  (let [operation (.operation diff)]
    (if flip-insert-delete?
      (flip-insert-delete operation)
      operation)))

(defn converter
  [flip-insert-delete?]
  (fn [diff]
    (diff-conversions (diff-operation flip-insert-delete? diff))))

(defn diff->string
  [diff-converter diff]
  (let [convert (diff-converter diff)]
    (convert (.text diff))))

(defn generate-string-diff
  [diff converter]
  (->> diff
       (map (partial diff->string converter))
       (apply str)))

(defn levenshtein-distance
  [diff]
  (.diff_levenshtein (diff_match_patch.) diff))

(defn string-similarity-percentage
  [diff a b]
  (let [longest (max (count a) (count b))
        distance (levenshtein-distance diff)]
    (long (* (/ (- longest distance)
                longest)
             100))))

(defn report-string-diff
  [diff a b]
  [(str "strings differ (" (string-similarity-percentage diff a b) "% similarity)")
   (str "expected: " (pr-str (generate-string-diff diff (converter true))))
   (str "actual:   " (pr-str (generate-string-diff diff (converter false))))])

(defrecord StringDiff [diff a b]
  Report
  (report [_] (report-string-diff diff a b)))

(defn diff-match-patch-string
  [a b]
  (.diff_main (diff_match_patch.) a b))

(defn diff-string
  [a b]
  (if (= (type b) String)
    [(StringDiff. (diff-match-patch-string a b) a b)]
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
