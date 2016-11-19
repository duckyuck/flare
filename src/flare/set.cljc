(ns flare.set
  (:require [flare.util :refer [flatten-when-single]]))

(defn report-set
  [{:keys [only-in values]} & rest]
  [(case only-in
      :a
      (str "expected set to contain: "
           (pr-str (flatten-when-single values))
           ", but not found.")

      :b
      (str "set contained: "
           (pr-str (flatten-when-single values))
           ", but not expected."))])

(defn diff-set
  [a b]
  (let [only-in-a (clojure.set/difference a b)
        only-in-b (clojure.set/difference b a)]
    (-> []
        (cond-> (seq only-in-a)
          (conj {:type :set
                 :only-in :a
                 :values only-in-a}))
        (cond-> (seq only-in-b)
          (conj {:type :set
                 :only-in :b
                 :values only-in-b})))))
