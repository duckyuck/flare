(ns flare.indexed)

(defn indexed-diff? [m]
  (and (map? m)
       (= (:type m) :indexed)))

(defn flatten-indexed-diffs
  ([diff-or-coll]
   (flatten-indexed-diffs {} [] diff-or-coll))
  ([a ks diff-or-coll]
   (cond
     (indexed-diff? diff-or-coll)
     (reduce into
             {}
             (map (fn [[k v]] (flatten-indexed-diffs a (conj ks k) v))
                     (:diff diff-or-coll)))

     (sequential? diff-or-coll)
     (let [groups (group-by indexed-diff? diff-or-coll)
           indexed-diffs (first (groups true))
           other-diffs (groups false)]
       (-> a
           (cond-> indexed-diffs (flatten-indexed-diffs ks indexed-diffs))
           (cond-> (seq other-diffs) (assoc ks other-diffs))))

     :else (throw (ex-info "diff-or-coll must be vector or map" diff-or-coll)))))

(defn join-with-newlines [coll]
  (clojure.string/join "\n" coll))

(defn report-indexed-diff-entry [[path diffs] report]
  (let [reports (flatten (map report diffs))
        indent-reports? (and (seq path) (< 1 (count reports)))]
    (-> reports
        (cond->> indent-reports? (map #(str "  " %)))
        join-with-newlines
        (cond->> (seq path) (str "in " (pr-str path) (if indent-reports? "\n" " "))))))

(defn report-indexed-diff [diff report]
  (->> diff
       flatten-indexed-diffs
       sort
       (map #(report-indexed-diff-entry % report))))

(defn indexed-diff [diff]
  {:type :indexed
   :diff diff})
