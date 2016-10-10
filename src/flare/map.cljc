(ns flare.map
  (:require [clojure.set :as set]
            [flare.indexed :as indexed]
            [flare.util :refer [pluralize flatten-when-single]]))

(defn report-keys-diff
  [{:keys [only-in keys]} & rest]
  (case only-in
    :a [(str "expected map to contain " (pluralize keys "key") ": "
             (pr-str (flatten-when-single keys))
             ", but not found.")]
    :b [(str "map contained " (pluralize keys "key") ": "
             (pr-str (flatten-when-single keys))
             ", but not expected.")]))

(defn diff-keys
  [a b _]
  (let [a-keys (set (keys a))
        b-keys (set (keys b))]
    (when (not= a-keys b-keys)
      (let [only-in-a (set/difference a-keys b-keys)
            only-in-b (set/difference b-keys a-keys)]
        (-> []
            (cond-> (seq only-in-a)
              (conj {:type :keys :only-in :a :keys only-in-a}))
            (cond-> (seq only-in-b)
              (conj {:type :keys :only-in :b :keys only-in-b})))))))

(defn diff-values
  [a b diff-fn]
  [(indexed/indexed-diff
    (->> (set/intersection (set (keys a)) (set (keys b)))
         (map (fn [k] [k (diff-fn (get a k) (get b k))]))
         (filter second)
         (into {})))])
