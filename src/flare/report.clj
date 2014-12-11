(ns flare.report)

(defprotocol Report
  (report [diff]))

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
