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

(defprotocol Pluralizable
  (pluralize [o noun]))

(extend-protocol Pluralizable
  java.util.Collection
  (pluralize [this noun]
    (pluralize (count this) noun))

  java.lang.Integer
  (pluralize [this noun]
    (pluralize (long this) noun))

  java.lang.Long
  (pluralize [this noun]
    (if (> this 1)
      (str noun "s")
      noun)))

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
    (generate-reports* diffs)
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
    (str "expected map to contain " (pluralize only-in-a "key") ": "
         (pr-str (flatten-when-single only-in-a))
         ", but not found.")
    (str "map contained " (pluralize only-in-b "key") ": "
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
       (str "actual is missing " missing-count " " (pluralize only-in-a "element") ": " (pr-str only-in-a))
       (str "actual has " missing-count " " (pluralize only-in-b "element") " in excess: " (pr-str only-in-b)))]))

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

(defn diff-match-patch-string
  [a b]
  (let [dmp (diff_match_patch.)
        diff (.diff_main dmp a b)]
    (.diff_cleanupSemantic dmp diff)
    diff))

(def operation->keyword
  {diff_match_patch$Operation/EQUAL :equal
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
    {:a b
     :b b
     :a->b a->b
     :b->a b->a
     :differences-count (count-differences a->b)
     :similarity (string-similarity a b)}))

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
