(ns flare.map
  (:require [flare.diff :refer [diff*]]
            [flare.report :refer [Report]]
            [flare.util :refer [pluralize flatten-when-single]]
            [clojure.set :as set]))

(defn report-keys-diff
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
  (report [_] (report-keys-diff only-in-a only-in-b)))

(defn diff-keys
  [a b]
  (let [a-keys (set (keys a))
        b-keys (set (keys b))]
    (when (not= a-keys b-keys)
      (let [only-in-a (set/difference a-keys b-keys)
            only-in-b (set/difference b-keys a-keys)]
        (-> []
            (cond-> (seq only-in-a) (conj (MapKeysDiff. only-in-a nil)))
            (cond-> (seq only-in-b) (conj (MapKeysDiff. nil only-in-b))))))))

(defn diff-values
  [a b]
  (->> (set/intersection (set (keys a)) (set (keys b)))
       (map (fn [k] [k (diff* (get a k) (get b k))]))
       (filter second)
       (into {})
       vector))
