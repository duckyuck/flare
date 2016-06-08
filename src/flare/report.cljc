(ns flare.report
  (:require [flare.atom :as atom]
            [flare.string :as string]
            [flare.indexed :as indexed]
            [flare.map :as map]
            [flare.sequential :as sequential]
            [flare.set :as set]))

(def reporters
  {:sequential sequential/report-size-diff
   :map        map/report-keys-diff
   :set        set/report-set
   :string     string/report-string-diff
   :atom       atom/report-atom-diff
   :indexed    indexed/report-indexed-diff})

(declare report*)

(defn report-diff [diff]
  (let [reporter (-> diff :type reporters)]
    (reporter diff report*)))

(defn report* [diff]
  (mapcat report-diff (if (sequential? diff)
                        diff
                        (vector diff))))

(defn report
  [diff]
  (try
    (report* diff)
    (catch Exception e)))
