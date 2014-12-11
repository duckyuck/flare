(ns flare.string
  (:require [flare.atom :refer [diff-atom]]
            [flare.diff :refer [diff*]]
            [flare.report :refer [Report]]
            [flare.util :refer [pluralize]])
  (:import [name.fraser.neil.plaintext diff_match_patch]
           [name.fraser.neil.plaintext diff_match_patch$Operation]))

(defn diff-match-patch-string
  [a b]
  (let [dmp (diff_match_patch.)
        diff (.diff_main dmp a b)]
    (.diff_cleanupSemantic dmp diff)
    diff))

(def operation->keyword
  {diff_match_patch$Operation/EQUAL  :equal
   diff_match_patch$Operation/INSERT :insert
   diff_match_patch$Operation/DELETE :delete})

(defn diff->tuple
  [diff]
  [(operation->keyword (.operation diff)) (.text diff)])

(defn partition-between
  [pred? coll]
  (->> (map pred? coll (rest coll))
       (reductions not= true)
       (map list coll)
       (partition-by second)
       (map (partial map first))))

(defn insert-operation?
  [[operation _]]
  (= operation :insert))

(defn delete-operation?
  [[operation _]]
  (= operation :delete))

(defn equal-operation?
  [[operation _]]
  (= operation :equal))

(defn change-operation?
  [diff]
  (or (insert-operation? diff) (delete-operation? diff)))

(defn change-operations?
  [prev curr]
  (and (change-operation? prev) (change-operation? curr)))

(defn find-first
  [pred coll]
  (first (filter pred coll)))

(defn consolidate-diffs
  [diffs]
  (if (= 1 (count diffs))
    (first diffs)
    [:change [(find-first insert-operation? diffs)
              (find-first delete-operation? diffs)]]))

(defn diff-tuples
  [a b]
  (->> (diff-match-patch-string a b)
       (map diff->tuple)
       (partition-between (complement change-operations?))
       (map consolidate-diffs)))

(defn count-differences
  [diff-tuples]
  (->> diff-tuples
       (remove equal-operation?)
       count))

(defn levenshtein-distance
  [diff]
  (.diff_levenshtein (diff_match_patch.) diff))

(defn string-similarity
  [a b]
  (let [longest (max (count a) (count b))
        distance (levenshtein-distance (diff-match-patch-string a b))]
    (/ (- longest distance)
       longest)))

(defn create-string-diff
  [a b]
  (let [a->b (diff-tuples a b)
        b->a (diff-tuples b a)]
    {:a                 b
     :b                 b
     :a->b              a->b
     :b->a              b->a
     :differences-count (count-differences a->b)
     :similarity        (string-similarity a b)}))

(def render-tuple-dispatch first)

(defmulti render-tuple #'render-tuple-dispatch)

(defn enclose-in-parenthesis
  [s]
  (str "(" s ")"))

(defn dashes
  [n]
  (apply str (repeat n "-")))

(defn string->dashes
  [s]
  (-> s count dashes))

(defmethod render-tuple :insert
  [[_ s]]
  (-> s
      string->dashes
      enclose-in-parenthesis))

(defmethod render-tuple :delete
  [[_ s]]
  (enclose-in-parenthesis s))

(defn append-dashes
  [s max]
  (->> s
       count
       (- max)
       dashes
       (str s)))

(defmethod render-tuple :change
  [[_ [[_ insert] [_ delete]]]]
  (-> delete
      (append-dashes (count insert))
      enclose-in-parenthesis))

(defmethod render-tuple :default
  [[_ s]]
  s)

(defn diff-tuples->string
  [diff-tuples]
  (->> diff-tuples
       (map render-tuple)
       (apply str)))

(defn fraction->percent
  [n]
  (long (* n 100)))

(defn report-string-diff
  [{:keys [a->b b->a differences-count similarity]}]
  [(str "strings have " differences-count (pluralize differences-count " difference") " "
        "(" (fraction->percent similarity)  "% similarity)")
   (str "expected: " (pr-str (diff-tuples->string a->b)))
   (str "actual:   " (pr-str (diff-tuples->string b->a)))])

(defrecord StringDiff [diff]
  Report
  (report [_] (report-string-diff diff)))

(defn diff-string
  [a b]
  (if (= (type b) String)
    [(StringDiff. (create-string-diff a b))]
    (diff-atom a b)))
