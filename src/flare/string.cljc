(ns flare.string
  (:require [flare.util :refer [pluralize]]
            #?(:cljs cljsjs.google-diff-match-patch))
  #?(:clj (:import [name.fraser.neil.plaintext diff_match_patch]
                   [name.fraser.neil.plaintext diff_match_patch$Operation])))

(def operation->keyword
  #?(:clj {diff_match_patch$Operation/DELETE :delete
           diff_match_patch$Operation/INSERT :insert
           diff_match_patch$Operation/EQUAL  :equal}
     :cljs {-1 :delete
            0 :equal
            1 :insert}))

#?(:clj
   (defn diff->tuple
     [diff]
     [(operation->keyword (.operation diff)) (.text diff)]))

(defn create-dmp []
  #?(:clj (diff_match_patch.)
     :cljs (js/diff_match_patch.)))

(defn dmp-diff->tuples [diff]
  #?(:clj (map diff->tuple diff)
     :cljs (->> diff
                js->clj
                (map #(update % 0 operation->keyword)))))

(defn diff-match-patch-string [a b]
  (let [dmp (create-dmp)
          diff (.diff_main dmp a b)]
     (.diff_cleanupSemantic dmp diff)
     diff))

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

(defn consolidate-tuples
  [diff-tuples]
  (if (= 1 (count diff-tuples))
    (first diff-tuples)
    [:change [(find-first insert-operation? diff-tuples)
              (find-first delete-operation? diff-tuples)]]))

(defn partition-between
  [pred? coll]
  (->> (map pred? coll (rest coll))
       (reductions not= true)
       (map list coll)
       (partition-by second)
       (map (partial map first))))

(defn consolidate
  [diff-tuples]
  (->> diff-tuples
       (partition-between (complement change-operations?))
       (mapv consolidate-tuples)))

(defn add-context
  [diff-tuples]
  (-> diff-tuples
      (update-in [0] conj :first)
      (update-in [(dec (count diff-tuples))] conj :last)))

(defn diff-tuples
  [a b]
  (->> (diff-match-patch-string a b)
       dmp-diff->tuples
       consolidate
       add-context))

(defn count-differences
  [diff-tuples]
  (->> diff-tuples
       (remove equal-operation?)
       count))

(defn levenshtein-distance
  [diff]
  (.diff_levenshtein (create-dmp) diff))

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
    {:type :string
     :a                 b
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

(defn append-str
  [s n to-append]
  (str (apply str (take n to-append)) s))

(defn prepend-str
  [s n to-prepend]
  (str s (apply str (take-last n to-prepend))))

(defmethod render-tuple :equal
  [[_ s context]]
  (if (> (count s) 10)
    (-> "..."
        (cond-> (not= context :first) (append-str 6 s))
        (cond-> (not= context :last) (prepend-str 6 s)))
    s))

(defn diff-tuples->string
  [diff-tuples]
  (->> diff-tuples
       (map render-tuple)
       (apply str)))

(defn fraction->percent
  [n]
  (long (* n 100)))

(defn report-string-diff
  [{:keys [a->b b->a differences-count similarity]} & rest]
  [(str "strings have " differences-count (pluralize differences-count " difference") " "
        "(" (fraction->percent similarity)  "% similarity)")
   (str "expected: " (pr-str (diff-tuples->string a->b)))
   (str "actual:   " (pr-str (diff-tuples->string b->a)))])

(defn diff-string
  [a b _]
  [(let [a->b (diff-tuples a b)
         b->a (diff-tuples b a)]
     {:type :string
      :a->b              a->b
      :b->a              b->a
      :differences-count (count-differences a->b)
      :similarity        (string-similarity a b)})])

